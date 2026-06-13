package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.ManutencaoService;
import pt.kartodromo.core.model.enums.TipoManutencao;

import java.time.LocalDate;

@Controller
@RequestMapping("/manutencao")
public class ManutencaoController {

    private final ManutencaoService service = new ManutencaoService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",    session.getAttribute("authUser"));
        model.addAttribute("activePage",  "manutencao");
        model.addAttribute("historico",   service.listarTodas());
        model.addAttribute("emCurso",     service.listarEmCurso());
        model.addAttribute("revisoes",    service.listarProximasRevisoes(30));
        model.addAttribute("karts",       service.listarKarts());
        model.addAttribute("tipos",       TipoManutencao.values());
        return "manutencao";
    }

    @PostMapping("/registar")
    public String registar(@RequestParam("kartId") Long kartId,
                           @RequestParam("descricao") String descricao,
                           @RequestParam("tipo") String tipo,
                           @RequestParam(value = "proximaRevisao", required = false) String proximaRevisao,
                           @RequestParam(value = "bloquear", required = false) String bloquear,
                           RedirectAttributes ra) {
        try {
            LocalDate revisao = (proximaRevisao != null && !proximaRevisao.isBlank())
                    ? LocalDate.parse(proximaRevisao) : null;
            service.registar(kartId, descricao, TipoManutencao.valueOf(tipo), revisao, "sim".equals(bloquear));
            ra.addFlashAttribute("sucesso", "Manutenção registada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/manutencao";
    }

    @PostMapping("/concluir")
    public String concluir(@RequestParam("id") Long id,
                           @RequestParam(value = "libertar", required = false) String libertar,
                           RedirectAttributes ra) {
        try {
            service.concluir(id, "sim".equals(libertar));
            ra.addFlashAttribute("sucesso", "Manutenção concluída.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/manutencao";
    }
}
