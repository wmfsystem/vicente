/* Arquivo gerado utilizando VICGERADOR por munif as 28/02/2018 01:55:19 */
/* Para não gerar o arquivo novamente coloque na primeira linha um comentário com  VICIGNORE , pode ser essa mesmo */
package br.com.munif.framework.vicente.security.service.profile;

import br.com.munif.framework.vicente.application.BaseService;
import br.com.munif.framework.vicente.application.VicRepository;
import br.com.munif.framework.vicente.security.domain.profile.Operation;
import br.com.munif.framework.vicente.security.service.interfaces.IOperationService;
import org.springframework.stereotype.Service;

/**
 * @author GeradorVicente
 */
@Service
public class OperationService extends BaseService<Operation> implements IOperationService {
    public OperationService(VicRepository<Operation> repository) {
        super(repository);
    }
}
