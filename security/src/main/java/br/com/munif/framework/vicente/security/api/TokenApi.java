/* Arquivo gerado utilizando VICGERADOR por munif as 28/02/2018 02:07:54 */
/* Para não gerar o arquivo novamente coloque na primeira linha um comentário com  VICIGNORE , pode ser essa mesmo */
package br.com.munif.framework.vicente.security.api;

import br.com.munif.framework.vicente.api.BaseAPI;
import br.com.munif.framework.vicente.security.domain.Token;
import br.com.munif.framework.vicente.security.domain.dto.LoginDto;
import br.com.munif.framework.vicente.security.domain.dto.LoginResponseDto;
import br.com.munif.framework.vicente.security.service.interfaces.ITokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author GeradorVicente
 */
@RestController
@RequestMapping("/api/token")
public class TokenApi extends BaseAPI<Token> {

    private final Logger log = LogManager.getLogger(TokenApi.class);
    private static final String ENTITY_NAME = "token";
    private final ITokenService tokenService;

    public TokenApi(ITokenService service) {
        super(service);
        this.tokenService = (ITokenService) service;
    }

    @Transactional
    @RequestMapping(value = "/login/bypassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LoginResponseDto loga(@RequestBody LoginDto login) {
        return tokenService.login(login);
    }

    @Transactional
    @RequestMapping(value = "/login/bygoogle", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LoginResponseDto logaGoogle(@RequestBody String token) {
        return tokenService.loginOnGoogle(token);
    }

    @Transactional
    @RequestMapping(value = "/sigin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LoginResponseDto sigin(@RequestBody LoginDto login) {
        return tokenService.sigin(login);
    }

    @Transactional
    @RequestMapping(value = "/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LoginResponseDto logout() {
        return tokenService.logout();
    }

    @Transactional
    @RequestMapping(value = "/login/bypassword/{login}/{senha:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LoginResponseDto logaGet(@PathVariable String login, @PathVariable String senha) {
        return loga(new LoginDto(login, senha));
    }

    @Transactional
    @GetMapping(value = "/recover-password/{id:.+}")
    public ResponseEntity<Void> recoverPassword(@PathVariable("id") String id) {
        tokenService.recoverPassword(id);
        return ResponseEntity.noContent().build();
    }
}
