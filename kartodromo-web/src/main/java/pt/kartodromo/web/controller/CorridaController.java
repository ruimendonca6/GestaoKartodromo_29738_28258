package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/corridas")
public class CorridaController {

    private final CorridaService       service          = new CorridaService();
    private final ClienteService       clienteService   = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",   session.getAttribute("authUser"));
        model.addAttribute("activePage", "corridas");
        model.addAttribute("corridas",   service.listarCorridas());
        model.addAttribute("clientes",   clienteService.listarClientes());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "corridas";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("dataHoraInicio") String dataHoraInicio,
                        @RequestParam("duracaoMinutos") int duracaoMinutos,
                        @RequestParam("vagasMaximas") int vagasMaximas,
                        @RequestParam("categoriaId") Long categoriaId,
                        @RequestParam("clienteId") Long clienteId,
                        @RequestParam("layoutNome") String layoutNome,
                        RedirectAttributes ra) {
        try {
            service.criarCorrida(
                    LocalDateTime.parse(dataHoraInicio),
                    duracaoMinutos, vagasMaximas,
                    categoriaId, clienteId, layoutNome);
            ra.addFlashAttribute("sucesso", "Corrida criada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/corridas";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("id") Long id,
                            @RequestParam("dataHoraInicio") String dataHoraInicio,
                            @RequestParam("duracaoMinutos") int duracaoMinutos,
                            @RequestParam("vagasMaximas") int vagasMaximas,
                            @RequestParam("categoriaId") Long categoriaId,
                            @RequestParam("clienteId") Long clienteId,
                            @RequestParam("layoutNome") String layoutNome,
                            RedirectAttributes ra) {
        try {
            service.atualizarCorrida(id,
                    LocalDateTime.parse(dataHoraInicio),
                    duracaoMinutos, vagasMaximas,
                    categoriaId, clienteId, layoutNome);
            ra.addFlashAttribute("sucesso", "Corrida atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/corridas";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.eliminarCorrida(id);
            ra.addFlashAttribute("sucesso", "Corrida eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/corridas";
    }
}
