@RestController
public class HelloController {
  @GetMapping("/hello")
  public String hello() {
    return "Hello, Inventory!";
  }
}
