package br.com.fiap.zelacidade.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de identificação da API.
 * DICA PARA O VÍDEO: altere VERSAO e MENSAGEM em uma nova branch para
 * demonstrar a mudança no código-fonte publicada na nuvem.
 */
@RestController
public class InfoController {

    static final String APP = "ZelaCidade";
    static final String VERSAO = "1.0.0";
    static final String MENSAGEM = "Bem-vindo ao ZelaCidade - zeladoria colaborativa dos espaços públicos";

    @GetMapping("/")
    public Map<String, String> info() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("app", APP);
        info.put("versao", VERSAO);
        info.put("mensagem", MENSAGEM);
        info.put("documentacao", "/swagger-ui.html");
        return info;
    }
}
