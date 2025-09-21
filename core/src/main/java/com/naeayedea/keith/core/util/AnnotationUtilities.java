/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class AnnotationUtilities {

    @Nullable
    public static <A extends Annotation> A findAnnotationOnClass(@NonNull Class<?> clazz, @NonNull Class<A> annotationToFind) {
        if (clazz.isAnnotationPresent(annotationToFind)) {
            return clazz.getAnnotation(annotationToFind);
        }

        return findAnnotationInClassRecursive(clazz, annotationToFind, new HashSet<>());
    }

    @Nullable
    private static <A extends Annotation> A findAnnotationInClassRecursive(@NonNull Class<?> clazz, @NonNull Class<A> annotationToFind, @NonNull Set<Class<? extends Annotation>> visited) {
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annType = annotation.annotationType();

            if (annType == annotationToFind) {
                visited.add(annType);

                AnnotationUtils.findAnnotation(clazz, annType);

                //noinspection unchecked
                return (A) annotation;
            }

            if (visited.add(annType)) {
                A result = findAnnotationInClassRecursive(annType, annotationToFind, visited);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
