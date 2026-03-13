package com.paravai.communities.membership.application.command.createfounder;

import com.paravai.communities.membership.application.event.MembershipEventFactory;
import com.paravai.communities.membership.application.snapshot.MembershipSnapshotSupport;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domain.value.OperationTypeValue;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CreateFounderMembershipService {

    private static final Logger log =
            LoggerFactory.getLogger(CreateFounderMembershipService.class);

    private final MembershipRepository repo;
    private final NonBlockingEventPublisher eventPublisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;

    public CreateFounderMembershipService(
            MembershipRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Membership> snapshotMapper,
            MembershipEventFactory eventFactory
    ) {
        this.repo = Objects.requireNonNull(repo, "repo is required");
        this.eventPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher is required"),
                log
        );
        this.snapshots = new MembershipSnapshotSupport(
                Objects.requireNonNull(snapshotMapper, "snapshotMapper is required")
        );
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory is required");
    }

    public Mono<Void> createFounderMembership(CreateFounderMembershipRequest request) {
        Objects.requireNonNull(request, "request is required");

        return repo.findByTenantIdAndCommunityIdAndUserId(
                        request.tenantId(),
                        request.communityId(),
                        request.founderUserId()
                )
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        log.debug(
                                "[{}] Founder membership already exists for community {} and user {}",
                                request.traceId(),
                                request.communityId().value(),
                                request.founderUserId().value()
                        );
                        return Mono.empty();
                    }

                    Membership membership = MembershipFactory.createFounder(
                            request.tenantId(),
                            request.communityId(),
                            request.founderUserId()
                    );

                    return repo.save(membership)
                            .flatMap(saved -> {
                                log.info(
                                        "[{}] Founder membership {} created for community {} and user {}",
                                        request.traceId(),
                                        saved.id().value(),
                                        request.communityId().value(),
                                        request.founderUserId().value()
                                );

                                var snapshot = snapshots.snapshot(saved);

                                EntityChangedEvent evt = eventFactory.build(
                                        OperationTypeValue.CREATED,
                                        saved.id(),
                                        request.traceId(),
                                        request.founderUserId().value(),
                                        request.sourceSystem(),
                                        "Founder membership created after community creation",
                                        null,
                                        snapshot
                                );

                                return eventPublisher.publish(evt).then();
                            });
                });
    }
}