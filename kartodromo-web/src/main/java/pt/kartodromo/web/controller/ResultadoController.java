package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.ResultadoService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/resultados")
public class ResultadoController {

    private final ResultadoService service = new ResultadoService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",       session.getAttribute("authUser"));
        model.addAttribute("activePage",     "resultados");
        model.addAttribute("resultados",     service.listarTodos());
        model.addAttribute("melhorVolta",    service.listarMelhorVoltaPorPiloto());
        model.addAttribute("classificacao",  service.listarClassificacaoGeral());
        return "resultados";
    }

    @PostMapping("/registar")
    public String registar(@RequestParam("nomePiloto") String nomePiloto,
                           @RequestParam("nomeCorrida") String nomeCorrida,
                           @RequestParam("minutos") int minutos,
                           @RequestParam("segundos") int segundos,
                           @RequestParam("milissegundos") int milissegundos,
                           @RequestParam("posicao") int posicao,
                           RedirectAttributes ra) {
        try {
            long tempoMs = (minutos * 60_000L) + (segundos * 1000L) + milissegundos;
            service.registar(nomePiloto, nomeCorrida, tempoMs, posicao, LocalDateTime.now());
            ra.addFlashAttribute("sucesso", "Resultado registado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/resultados";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.eliminar(id);
            ra.addFlashAttribute("sucesso", "Resultado removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/resultados";
    }
}
