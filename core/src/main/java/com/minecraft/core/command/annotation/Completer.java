package com.minecraft.core.command.annotation;

import com.minecraft.core.account.context.objects.rank.type.RankType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Completer {

    String name();

    String[] subCommands() default {};

    RankType rank() default RankType.MEMBER;
}
