package pt.kartodromo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.model.enums.KartEstado;

@Controller
@RequestMapping("/karts")
public class KartController {

    private final KartService kartService = new KartService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser", session.getAttribute("authUser"));
        model.addAttribute("activePage", "karts");

        model.addAttribute("karts", kartService.listarKarts());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("estados", KartEstado.values());

        return "karts";
    }

    @PostMapping("/criar")
    public String criar(
            @RequestParam("numero") int numero,
            @RequestParam("estado") String estado,
            @RequestParam("disponivel") boolean disponivel,
            @RequestParam("categoriaId") Long categoriaId,
            RedirectAttributes ra) {

        try {
            kartService.criarKart(
                    numero,
                    KartEstado.valueOf(estado),
                    disponivel,
                    categoriaId
            );

            ra.addFlashAttribute("sucesso", "Kart criado com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/karts";
    }

    @PostMapping("/estado")
    public String atualizarEstado(
            @RequestParam("id") Long id,
            @RequestParam("estado") String estado,
            RedirectAttributes ra) {

        try {
            kartService.atualizarEstado(id, KartEstado.valueOf(estado));
            ra.addFlashAttribute("sucesso", "Estado do kart atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/karts";
    }

    @PostMapping("/disponibilidade")
    public String atualizarDisponibilidade(
            @RequestParam("id") Long id,
            @RequestParam("disponivel") boolean disponivel,
            RedirectAttributes ra) {

        try {
            kartService.definirDisponibilidade(id, disponivel);
            ra.addFlashAttribute("sucesso", "Disponibilidade do kart atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/karts";
    }
}