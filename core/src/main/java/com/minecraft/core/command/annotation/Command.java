package com.minecraft.core.command.annotation;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.command.platform.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();

    String[] aliases() default {};

    String description() default "";

    String usage() default "";

    String permission() default "";

    RankType rank() default RankType.MEMBER;

    Platform platform() default Platform.BOTH;

    boolean onlyPlayer() default true;

    boolean runAsync() default false;
}
