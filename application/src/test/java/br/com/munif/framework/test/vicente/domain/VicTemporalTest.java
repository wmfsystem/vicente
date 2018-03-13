package br.com.munif.framework.test.vicente.domain;

import br.com.munif.framework.test.vicente.application.MySQLSpringConfig;
import br.com.munif.framework.test.vicente.application.PontoService;
import br.com.munif.framework.test.vicente.application.SalarioRepository;
import br.com.munif.framework.test.vicente.application.SalarioService;
import br.com.munif.framework.test.vicente.domain.model.Pessoa;
import br.com.munif.framework.test.vicente.domain.model.Ponto;
import br.com.munif.framework.test.vicente.domain.model.Salario;
import br.com.munif.framework.vicente.core.RightsHelper;
import br.com.munif.framework.vicente.core.VicThreadScope;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MySQLSpringConfig.class})
public class VicTemporalTest {

    private static int contador = 0;

    @Autowired
    private SalarioRepository repository;

    @Autowired
    private SalarioService service;

    @Before
    @Transactional
    public void setUp() {
        contador++;
        VicThreadScope.ui.set("USUARIO" + contador);
        VicThreadScope.gi.set("GRUPO" + contador);
        VicThreadScope.oi.set(contador + ".");
        VicThreadScope.defaultRights.set(RightsHelper.OWNER_ALL | RightsHelper.GROUP_ALL);
        for (long i = 1000; i < 100000; i += 1000) {
            Salario s = new Salario("Munif " + i, BigDecimal.valueOf(i));
            s.setStartTime(i);
            s.setEndTime(i + 999);
            Salario ss = repository.saveAndFlush(s);
        }
    }

    @Test
    @Transactional
    public void recupera1() {
        VicThreadScope.effectiveTime.set(55000l);
        List<Salario> todos = service.findAll();
        assertEquals(1, todos.size());
    }

    @Test
    @Transactional
    public void recupera0a() {
        VicThreadScope.effectiveTime.set(100000l);
        List<Salario> todos = service.findAll();
        assertEquals(0, todos.size());
    }

    @Test
    @Transactional
    public void recupera0b() {
        contador++;
        VicThreadScope.ui.set("USUARIO" + contador);
        VicThreadScope.gi.set("GRUPO" + contador);
        VicThreadScope.oi.set(contador + ".");

        VicThreadScope.effectiveTime.set(55030l);
        List<Salario> todos = service.findAll();
        assertEquals(0, todos.size());
    }

}