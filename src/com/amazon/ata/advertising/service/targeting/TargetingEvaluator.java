package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;
    ExecutorService executorService = Executors.newCachedThreadPool();
    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
        List<Future<Boolean>> futures = new ArrayList<>();
        for (TargetingPredicate targetingPredicate : targetingGroup.getTargetingPredicates()) {
            futures.add(executorService.submit(() -> !targetingPredicate.evaluate(requestContext).isTrue()));
        }
        for (Future<Boolean> future : futures) {
            try {
                if (future.get()) {
                    return TargetingPredicateResult.FALSE;
                }
            } catch (Exception e) {
                return TargetingPredicateResult.FALSE;
            }
        }
        return TargetingPredicateResult.TRUE;
//        Optional<TargetingPredicate> result = targetingGroup
//                .getTargetingPredicates()
//                .stream().filter(targetingPredicate -> !targetingPredicate.evaluate(requestContext).isTrue())
//                .findFirst();
//        return result.isPresent() ? TargetingPredicateResult.FALSE: TargetingPredicateResult.TRUE;
    }
}
