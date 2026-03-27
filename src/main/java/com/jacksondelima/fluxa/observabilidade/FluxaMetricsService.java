package com.jacksondelima.fluxa.observabilidade;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FluxaMetricsService {

    private final Counter authCadastroSuccessCounter;
    private final Counter authLoginSuccessCounter;
    private final Counter authLoginFailureCounter;
    private final Counter tarefaCriadaCounter;
    private final Counter tarefaAtualizadaCounter;
    private final Counter tarefaExcluidaCounter;
    private final Counter rateLimitExceededCounter;
    private final Timer administracaoUsuariosTimer;
    private final Timer administracaoTarefasTimer;

    public FluxaMetricsService(MeterRegistry meterRegistry) {
        this.authCadastroSuccessCounter = Counter.builder("fluxa_auth_cadastro_success_total")
                .description("Total de cadastros de usuario com sucesso")
                .register(meterRegistry);
        this.authLoginSuccessCounter = Counter.builder("fluxa_auth_login_success_total")
                .description("Total de logins com sucesso")
                .register(meterRegistry);
        this.authLoginFailureCounter = Counter.builder("fluxa_auth_login_failure_total")
                .description("Total de falhas de login")
                .register(meterRegistry);
        this.tarefaCriadaCounter = Counter.builder("fluxa_tarefa_criada_total")
                .description("Total de tarefas criadas")
                .register(meterRegistry);
        this.tarefaAtualizadaCounter = Counter.builder("fluxa_tarefa_atualizada_total")
                .description("Total de tarefas atualizadas")
                .register(meterRegistry);
        this.tarefaExcluidaCounter = Counter.builder("fluxa_tarefa_excluida_total")
                .description("Total de tarefas excluidas")
                .register(meterRegistry);
        this.rateLimitExceededCounter = Counter.builder("fluxa_rate_limit_exceeded_total")
                .description("Total de bloqueios por rate limit")
                .register(meterRegistry);
        this.administracaoUsuariosTimer = Timer.builder("fluxa_administracao_usuarios_seconds")
                .description("Tempo de listagem administrativa de usuarios")
                .register(meterRegistry);
        this.administracaoTarefasTimer = Timer.builder("fluxa_administracao_tarefas_seconds")
                .description("Tempo de listagem administrativa de tarefas")
                .register(meterRegistry);
    }

    public void registrarCadastroComSucesso() {
        authCadastroSuccessCounter.increment();
    }

    public void registrarLoginComSucesso() {
        authLoginSuccessCounter.increment();
    }

    public void registrarFalhaDeLogin() {
        authLoginFailureCounter.increment();
    }

    public void registrarTarefaCriada() {
        tarefaCriadaCounter.increment();
    }

    public void registrarTarefaAtualizada() {
        tarefaAtualizadaCounter.increment();
    }

    public void registrarTarefaExcluida() {
        tarefaExcluidaCounter.increment();
    }

    public void registrarRateLimitExcedido() {
        rateLimitExceededCounter.increment();
    }

    public <T> T medirListagemUsuarios(ThrowingSupplier<T> supplier) {
        return medir(administracaoUsuariosTimer, supplier);
    }

    public <T> T medirListagemTarefas(ThrowingSupplier<T> supplier) {
        return medir(administracaoTarefasTimer, supplier);
    }

    private <T> T medir(Timer timer, ThrowingSupplier<T> supplier) {
        Timer.Sample sample = Timer.start();
        try {
            return supplier.get();
        } finally {
            sample.stop(timer);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get();
    }
}
