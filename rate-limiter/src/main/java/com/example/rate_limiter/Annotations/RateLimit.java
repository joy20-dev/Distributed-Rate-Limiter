package com.example.rate_limiter.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
public @interface RateLimit {
    int requests() default 15;

    int windowSeconds() default 60;

    StrategyType strategy() default StrategyType.FIXED_WINDOW;

    enum StrategyType {
        FIXED_WINDOW,
        SLIDING_WINDOW
    }

}
