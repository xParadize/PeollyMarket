package com.peolly.companymicroservice.services;


import com.peolly.companymicroservice.kafka.CompanyKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyKafkaProducer companyKafkaProducer;

//    @Transactional(readOnly = true)
//    public Optional<Company> getCompanyById(Long id) {
//        return companyRepository.findCompanyById(id);
//    }

//    @Transactional
//    @KafkaListener(topics = "send-get-company-by-id", groupId = "org-deli-queuing-product")
//    public void consumeGetCompanyByIdEvent(GetCompanyByIdEvent event) {
//        Optional<Company> company = getCompanyById(event.companyId());
//        companyKafkaProducer.sendGetCompanyByIdResponse(company, event.companyId());
//    }
//
//    @Transactional(readOnly = true)
//    public Organization findOrganizationByName(String name) {
//        return organizationRepository.findByName(name).
//                orElse(null);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<OrganizationMainPageInfo> showAllOrganizations(int page, int organizationsPerPage) {
//        Page<Organization> organizations = organizationRepository.findAll(PageRequest.of(page, organizationsPerPage));
//        List<OrganizationMainPageInfo> orgInfoList = new ArrayList<>();
//        for (Organization o : organizations) {
//            orgInfoList.add(convertOrgToOrgMainPageInfo(o));
//        }
//        return new PageImpl<>(orgInfoList);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isUserAlreadyHasOrganization(UUID userId) {
//        Optional<Organization> organization = organizationRepository.findByCreatorId(userId);
//        return organization.isPresent();
//    }
//
//    @Transactional
//    public void save(Organization organization) {
//        organizationRepository.save(organization);
//    }
//
//    private OrganizationMainPageInfo convertOrgToOrgMainPageInfo(Organization organization) {
//        OrganizationMainPageInfo orgPageInfo = OrganizationMainPageInfo.builder()
//                .organizationName(organization.getName())
//                .link(String.format("http://%s:%s/store?company=%s", hostName, hostPort, organization.getName()))
//                .build();
//        return orgPageInfo;
//    }
}

