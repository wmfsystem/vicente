/* Arquivo gerado utilizando VICGERADOR por munif as 28/02/2018 01:55:19 */
/* Para não gerar o arquivo novamente coloque na primeira linha um comentário com  VICIGNORE , pode ser essa mesmo */
package br.com.munif.framework.vicente.security.service.profile;

import br.com.munif.framework.vicente.application.BaseService;
import br.com.munif.framework.vicente.application.VicRepository;
import br.com.munif.framework.vicente.security.domain.profile.Operation;
import br.com.munif.framework.vicente.security.domain.profile.Software;
import br.com.munif.framework.vicente.security.service.interfaces.ISoftwareService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author GeradorVicente
 */
@Service
public class SoftwareService extends BaseService<Software> implements ISoftwareService {
    private final OperationService operationService;

    public SoftwareService(VicRepository<Software> repository, OperationService operationService) {
        super(repository);
        this.operationService = operationService;
    }

    @Override
    @Transactional
    public Software save(Software resource) {
        for (Operation operation : resource.getOperations()) {
            operation.setSoftware(resource);
            operation = operationService.save(operation);
        }
        return super.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public Software loadNoTenancy(String id) {
        Software software = super.loadNoTenancy(id);
        if (software != null) {
            Hibernate.initialize(software.getOperations());
        }
        return software;
    }
}
