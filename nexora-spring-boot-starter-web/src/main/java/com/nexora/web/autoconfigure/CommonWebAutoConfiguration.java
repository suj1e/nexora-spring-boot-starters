package com.nexora.web.autoconfigure;

import com.nexora.web.aspect.ResponseWrapperAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Common web auto-configuration.
 *
 * <p>Automatically enables:
 * <ul>
 *   <li>Response wrapper AOP - wraps all controller responses in {@link com.nexora.web.model.Result}</li>
 *   <li>Global exception handler - handles all exceptions and returns unified format</li>
 * </ul>
 *
 * @author sujie
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CommonWebAutoConfiguration {

    /**
     * Response wrapper aspect is automatically registered via component scanning.
     * No explicit bean registration needed.
     */
}
