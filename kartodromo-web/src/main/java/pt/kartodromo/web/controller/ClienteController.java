package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.ClienteService;

import java.time.LocalDate;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service = new ClienteService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",   session.getAttribute("authUser"));
        model.addAttribute("activePage", "clientes");
        model.addAttribute("clientes",   service.listarClientes());
        return "clientes";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("nome") String nome,
                        @RequestParam("email") String email,
                        @RequestParam("dataNascimento") String dataNascimento,
                        @RequestParam("nivelExperiencia") int nivelExperiencia,
                        RedirectAttributes ra) {
        try {
            service.criarCliente(nome, LocalDate.parse(dataNascimento), email, nivelExperiencia);
            ra.addFlashAttribute("sucesso", "Cliente criado com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("id") Long id,
                            @RequestParam("nome") String nome,
                            @RequestParam("email") String email,
                            @RequestParam("dataNascimento") String dataNascimento,
                            @RequestParam("nivelExperiencia") int nivelExperiencia,
                            RedirectAttributes ra) {
        try {
            service.atualizarCliente(id, nome, LocalDate.parse(dataNascimento), email, nivelExperiencia);
            ra.addFlashAttribute("sucesso", "Cliente atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.removerCliente(id);
            ra.addFlashAttribute("sucesso", "Cliente removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }
}
