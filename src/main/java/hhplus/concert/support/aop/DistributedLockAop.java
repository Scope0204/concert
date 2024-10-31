package hhplus.concert.support.aop;

import hhplus.concert.support.annotation.DistributedLock;
import hhplus.concert.support.parser.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @DistributedLock 어노테이션 선언 시 수행되는 aop 클래스입니다.
 * @DistributedLock 어노테이션의 파라미터 값을 가져와 분산락 획득 시도 그리고 어노테이션이 선언된 메서드를 실행합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;

    @Around("@annotation(hhplus.concert.support.annotation.DistributedLock)")
    public Object DistributedLock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // 락의 이름으로 RLock 인스턴스를 가져온다. Lock 의 고유 키 값은 '메서드 이름{userId}' 형식으로 지정.
        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);
        try {
            // 정의된 waitTime까지 Lock 획득을 시도한다, 정의된 leaseTime이 지나면 잠금을 해제하도록 한다.
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());  // (2)

            if (!available) {
                log.info("Lock 획득 실패={}", rLock);
                return false;
            }
            //DistributedLock 어노테이션이 선언된 메서드를 실행한다.
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            try {
                // 종료 시 무조건 락을 해제한다.
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock - serviceName: {}, lockKey: {}", method.getName(), key);
            }
        }
    }
}
