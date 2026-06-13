package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.CategoriaKartService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaKartService service = new CategoriaKartService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser", session.getAttribute("authUser"));
        model.addAttribute("activePage", "categorias");
        model.addAttribute("categorias", service.listarCategorias());
        return "categorias";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("cilindrada") int cilindrada,
                        @RequestParam("descricao") String descricao,
                        @RequestParam("idadeMinima") int idadeMinima,
                        @RequestParam("experienciaMinima") int experienciaMinima,
                        @RequestParam("precoBase") BigDecimal precoBase,
                        RedirectAttributes ra) {
        try {
            service.criarCategoria(cilindrada, descricao, idadeMinima, experienciaMinima, precoBase);
            ra.addFlashAttribute("sucesso", "Categoria criada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("id") Long id,
                            @RequestParam("cilindrada") int cilindrada,
                            @RequestParam("descricao") String descricao,
                            @RequestParam("idadeMinima") int idadeMinima,
                            @RequestParam("experienciaMinima") int experienciaMinima,
                            @RequestParam("precoBase") BigDecimal precoBase,
                            RedirectAttributes ra) {
        try {
            service.atualizarCategoria(id, cilindrada, descricao, idadeMinima, experienciaMinima, precoBase);
            ra.addFlashAttribute("sucesso", "Categoria atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.removerCategoria(id);
            ra.addFlashAttribute("sucesso", "Categoria removida.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
