//package com.peolly.companymicroservice.services;
//
//
//import lombok.RequiredArgsConstructor;
//import org.deli.emailmicroservice.MailService;
//import org.deli.emailmicroservice.MailType;
//import org.deli.organizationmicroservice.dto.OrganizationTicketDto;
//import org.deli.organizationmicroservice.models.Organization;
//import org.deli.organizationmicroservice.models.OrganizationTicket;
//import org.deli.organizationmicroservice.repositories.OrganizationTicketRepository;
//import org.deli.usermicroservice.model.User;
//import org.deli.usermicroservice.repositories.UserRepository;
//import org.deli.utilservice.exceptions.NoUserTicketException;
//import org.deli.utilservice.exceptions.TicketNotFoundException;
//import org.deli.utilservice.exceptions.UserAlreadyHasOrgTicketException;
//import org.deli.utilservice.exceptions.UserAlreadyHasOrganizationException;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class CompanyTicketService {
//
//    private final OrganizationTicketRepository organizationTicketRepository;
//    private final MailService mailService;
//    private final UserRepository usersRepository;
//    private final CompanyService organizationService;
//
//    @Transactional
//    public void createTicket(OrganizationTicketDto organizationTicketDto, UUID creatorId) {
//
//        boolean isUserAlreadyHasOrganization = organizationService.isUserAlreadyHasOrganization(creatorId);
//        if (isUserAlreadyHasOrganization) throw new UserAlreadyHasOrganizationException("");
//
//        boolean isUserAlreadyHasTicket = isUserAlreadyHasTicket(creatorId);
//        if (isUserAlreadyHasTicket) throw new UserAlreadyHasOrgTicketException("");
//
//        OrganizationTicket organizationTicket = convertOrgTicketDtoToOrgTicket(organizationTicketDto, creatorId);
//        organizationTicketRepository.save(organizationTicket);
//        sendTicketCreatedNotification(creatorId);
//    }
//
//    @Transactional(readOnly = true)
//    public void sendTicketCreatedNotification(UUID companyCreatorId) {
//        User creator = usersRepository.findById(companyCreatorId).orElse(null);
//        mailService.sendEmail(creator, MailType.COMPANY_TICKET_SEND);
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrganizationTicket> showAllTickets(int page, int ticketsPerPage) {
//        return organizationTicketRepository.findAll(PageRequest.of(page, ticketsPerPage)).getContent();
//    }
//
//    @Transactional(readOnly = true)
//    public OrganizationTicketDto showUserTicket(UUID userId) {
//        OrganizationTicket ticket = organizationTicketRepository.findByCreatorId(userId).orElse(null);
//        if (ticket == null) throw new NoUserTicketException();
//        return convertTicketToTicketDto(ticket);
//    }
//
//    @Transactional
//    public void approveTicket(Integer ticketId) {
//        OrganizationTicket ticket = organizationTicketRepository.findById(ticketId).orElse(null);
//        if (ticket == null) throw new TicketNotFoundException();
//        User creator = usersRepository.findById(ticket.getCreatorId()).orElse(null);
//        organizationService.save(convertTicketToOrganization(ticket));
//        mailService.sendTicketApproved(creator);
//        organizationTicketRepository.delete(ticket);
//    }
//
//    @Transactional(readOnly = true)
//    public void rejectTicket(String message, Integer ticketId) {
//        OrganizationTicket ticket = organizationTicketRepository.findById(ticketId).orElse(null);
//        if (ticket == null) throw new TicketNotFoundException();
//        User creator = usersRepository.findById(ticket.getCreatorId()).orElse(null);
//        mailService.sendTicketRejected(creator, message);
//        organizationTicketRepository.delete(ticket);
//    }
//
//    private boolean isUserAlreadyHasTicket(UUID userId) {
//        Optional<OrganizationTicket> organizationTicket = organizationTicketRepository.findByCreatorId(userId);
//        return organizationTicket.isPresent();
//    }
//
//    private Organization convertTicketToOrganization(OrganizationTicket ticket) {
//        Organization organizationToSave = Organization.builder()
//                .name(ticket.getName())
//                .description(null)
//                .inn(ticket.getInn())
//                .requisites(ticket.getRequisites())
//                .creatorId(ticket.getCreatorId())
//                .registratedAt(LocalDateTime.now())
//                .build();
//        return organizationToSave;
//    }
//
//    private OrganizationTicketDto convertTicketToTicketDto(OrganizationTicket ticket) {
//        OrganizationTicketDto dto = OrganizationTicketDto.builder()
//                .name(ticket.getName())
//                .inn(ticket.getInn())
//                .requisites(ticket.getRequisites())
//                .build();
//        return dto;
//    }
//
//    private OrganizationTicket convertOrgTicketDtoToOrgTicket(OrganizationTicketDto dto, UUID creatorId) {
//        OrganizationTicket ticket = OrganizationTicket.builder()
//                .name(replaceWhitespacesInCompanyName(dto.getName()))
//                .inn(dto.getInn())
//                .requisites(dto.getRequisites())
//                .createdAt(LocalDateTime.now())
//                .creatorId(creatorId)
//                .build();
//        return ticket;
//    }
//
//    private String replaceWhitespacesInCompanyName(String name) {
//        return name.replaceAll(" ", "-");
//    }
//}
