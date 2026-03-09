package com.paravai.communities.membership.application.command.create;

import com.paravai.communities.membership.application.event.MembershipEventFactory;
import com.paravai.communities.membership.application.snapshot.MembershipSnapshotSupport;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.OperationTypeValue;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CreateInitialMembershipOnCommunityCreatedService {

    private static final Logger log =
            LoggerFactory.getLogger(CreateInitialMembershipOnCommunityCreatedService.class);

    private final MembershipRepository repo;
    private final NonBlockingEventPublisher eventPublisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;

    public CreateInitialMembershipOnCommunityCreatedService(
            MembershipRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Membership> snapshotMapper,
            MembershipEventFactory eventFactory
    ) {

        this.repo = Objects.requireNonNull(repo);
        this.eventPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new MembershipSnapshotSupport(
                Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory);
    }

    /**
     * Creates the initial ADMIN membership when a Community is created.
     */
    public Mono<Void> createAdminMembership(
            IdValue tenantId,
            IdValue communityId,
            IdValue creatorUserId,
            String traceId,
            String sourceSystem
    ) {

        return repo.findByTenantIdAndCommunityIdAndUserId(
                        tenantId,
                        communityId,
                        creatorUserId
                )

                // idempotency guard
                .hasElement()

                .flatMap(exists -> {

                    if (exists) {
                        log.debug(
                                "[{}] Membership already exists for community {} and user {}",
                                traceId,
                                communityId,
                                creatorUserId
                        );
                        return Mono.empty();
                    }

                    Membership membership =
                            MembershipFactory.createAdmin(
                                    tenantId,
                                    communityId,
                                    creatorUserId
                            );

                    return repo.save(membership)
                            .flatMap(saved -> {

                                var snapshot = snapshots.snapshot(saved);

                                EntityChangedEvent evt = eventFactory.build(
                                        OperationTypeValue.CREATED,
                                        saved.id(),
                                        traceId,
                                        creatorUserId.value(),
                                        sourceSystem,
                                        "Initial ADMIN membership created",
                                        null,
                                        snapshot
                                );

                                return eventPublisher.publish(evt).then();
                            });
                });
    }
}