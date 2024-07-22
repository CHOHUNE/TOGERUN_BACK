package com.example.simplechatapp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@Target({java.lang.annotation.ElementType.METHOD}) //메서드 단위에 붙여서 사용
public @interface NeedNotify {

}
