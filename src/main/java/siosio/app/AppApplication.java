package siosio.app;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@SpringBootApplication
@Controller
public class AppApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    private final TestTableRepository testTableRepository;
    
    public AppApplication(final TestTableRepository repository) {
        testTableRepository = repository;
    }

    
    @GetMapping("/")
    public String index(Model model) {
        final List<TestTable> list = testTableRepository.findAll(Sort.by("id"));
        model.addAttribute("list", list);
        return "input";
    }
    
    @PostMapping("/register")
    @Transactional
    public String register(TestTable table) {
        testTableRepository.save(table);
        return "redirect:/";
    }
}
