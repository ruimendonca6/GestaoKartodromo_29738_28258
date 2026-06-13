package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.PistaService;

@Controller
@RequestMapping("/pistas")
public class PistaController {

    private final PistaService service = new PistaService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",   session.getAttribute("authUser"));
        model.addAttribute("activePage", "pistas");
        model.addAttribute("pistas",     service.listarPistas());
        return "pistas";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("nome") String nome,
                        @RequestParam("comprimento") int comprimento,
                        @RequestParam("capacidade") int capacidade,
                        @RequestParam(value = "imagemPath", required = false) String imagemPath,
                        RedirectAttributes ra) {
        try {
            service.criarPista(nome, comprimento, capacidade, imagemPath);
            ra.addFlashAttribute("sucesso", "Pista criada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pistas";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("id") Long id,
                            @RequestParam("nome") String nome,
                            @RequestParam("comprimento") int comprimento,
                            @RequestParam("capacidade") int capacidade,
                            @RequestParam(value = "imagemPath", required = false) String imagemPath,
                            RedirectAttributes ra) {
        try {
            service.atualizarPista(id, nome, comprimento, capacidade, imagemPath);
            ra.addFlashAttribute("sucesso", "Pista atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pistas";
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.toggleAtiva(id);
            ra.addFlashAttribute("sucesso", "Estado da pista alterado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pistas";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.eliminarPista(id);
            ra.addFlashAttribute("sucesso", "Pista eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pistas";
    }
}
