CREATE TABLE tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    data_expiracao TIMESTAMP NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_tokens_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);