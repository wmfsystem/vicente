/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.framework.vicente.security.seed;

import br.com.munif.framework.vicente.core.RightsHelper;
import br.com.munif.framework.vicente.core.VicThreadScope;
import br.com.munif.framework.vicente.security.domain.Group;
import br.com.munif.framework.vicente.security.domain.Organization;
import br.com.munif.framework.vicente.security.domain.PasswordGenerator;
import br.com.munif.framework.vicente.security.domain.User;
import br.com.munif.framework.vicente.security.domain.profile.Operation;
import br.com.munif.framework.vicente.security.domain.profile.Software;
import br.com.munif.framework.vicente.security.repository.GroupRepository;
import br.com.munif.framework.vicente.security.repository.OrganizationRepository;
import br.com.munif.framework.vicente.security.repository.UserRepository;
import br.com.munif.framework.vicente.security.service.profile.SoftwareService;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author munif
 */
@Component
public class SeedSecurity {

    private final Logger log = Logger.getLogger("SeedSecurity");

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final OrganizationRepository organizationRepository;
    private final RequestMappingHandlerMapping handlerMapping;
    private final SoftwareService softwareService;

    public SeedSecurity(OrganizationRepository organizationRepository, GroupRepository groupRepository, UserRepository userRepository, RequestMappingHandlerMapping handlerMapping, SoftwareService softwareService) {
        this.organizationRepository = organizationRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.handlerMapping = handlerMapping;
        this.softwareService = softwareService;
    }

    public void seedSecurity() {
        if (userRepository.count() > 0) {
            return;
        }

        VicThreadScope.gi.set("SEED");
        VicThreadScope.ui.set("SEED");
        VicThreadScope.oi.set("SEED.");
        VicThreadScope.ip.set("127.0.0.1");
        VicThreadScope.defaultRights.set(RightsHelper.OWNER_ALL + RightsHelper.GROUP_READ_UPDATE);

        log.info("Inserting Security Data");

        Group g0 = new Group();
        g0.setCode("SEED");
        g0.setName("SEED");
        groupRepository.save(g0);

        Group g1 = new Group();
        g1.setCode("G1");
        g1.setName("Grupo 1");
        groupRepository.save(g1);
        Group g2 = new Group();
        g2.setCode("G2");
        g2.setName("Grupo 2");
        groupRepository.save(g2);

        Organization o1 = new Organization();
        o1.setCode("empresa");
        o1.setName("Empresa");
        organizationRepository.save(o1);

        Organization o2 = new Organization();
        o2.setCode("departamento");
        o2.setName("Departamento");
        o2.setUpper(o1);
        organizationRepository.save(o2);

        User admin = new User("admin@vicente.com.br", PasswordGenerator.generate("qwe123"));
        admin.setGroups(new HashSet<>());
        admin.getGroups().add(g0);
        admin.getGroups().add(g2);
        admin.setOrganizations(Collections.singleton(o1));
        admin = userRepository.save(admin);

        User user = new User("munif@vicente.com.br", PasswordGenerator.generate("qwe123"));
        user.setGroups(new HashSet<>());
        user.getGroups().add(g1);
        user.setOrganizations(Collections.singleton(o2));
        user = userRepository.save(user);

        Software software1 = new Software("VicSecurity", new HashSet<>());
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> hm : handlerMethods.entrySet()) {
            String apiName = hm.getValue().getBeanType().getName().substring(hm.getValue().getBeanType().getName().lastIndexOf(".") + 1);
            String method = hm.getValue().getMethod().getName();
            software1.getOperations().add(new Operation(apiName + "_" + method));
        }
        softwareService.save(software1);
    }

}
