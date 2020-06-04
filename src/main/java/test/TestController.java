package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  private final HostAccess hostAccess;
  private final Engine engine;
  private final String pyScript;
  private final String jsScript;

  public TestController(HostAccess hostAccess, Engine engine) throws IOException {
    this.hostAccess = hostAccess;
    this.engine = engine;
    Resource pythonResource = new ClassPathResource("test.py");
    Resource jsResource = new ClassPathResource("test.js");
    try (InputStream pyInputStream = pythonResource.getInputStream();
        InputStream jsInputStream = jsResource.getInputStream()) {
      pyScript = new String(pyInputStream.readAllBytes());
      jsScript = new String(jsInputStream.readAllBytes());
    }
  }


  @GetMapping("/test/python/{text}")
  public String testPython(@PathVariable("text") String text,
      @RequestParam(value = "oneContext", required = false) boolean oneContext) throws IOException {

    Context context = null;
    try {
      if (oneContext) {
        context = Context.getCurrent();
      } else {
        context = Context.newBuilder().allowHostAccess(hostAccess).engine(engine)
            .allowAllAccess(true).build();
        context.enter();
      }

      context.getPolyglotBindings().putMember(ScriptConstants.LANGUAGE_GLOBAL, "python");
      Value pythonValue = context.getBindings("python");
      Source pySource = Source.newBuilder("python", pyScript.replaceAll("%s", text), "test.py")
          .cached(false)
          .build();
      context.eval(pySource);
      Map<String, Object> resultMap = pythonValue.getMember("test_python_map").execute()
          .as(Map.class);
      List<Map<String, Object>> resultList = pythonValue.getMember("test_python_list").execute().as(
          List.class);
      String call_java_method = pythonValue.getMember("call_java_method").execute(new TestClass())
          .asString();
    } finally {
      if (!oneContext) {
        context.close();
      }
    }
    return "ok";
  }

  @GetMapping("/test/js/{text}")
  public String testJs(@PathVariable("text") String text,
      @RequestParam(value = "oneContext", required = false) boolean oneContext) throws IOException {
    Context context = null;
    try {
      if (oneContext) {
        context = Context.getCurrent();
      } else {
        context = Context.newBuilder().allowHostAccess(hostAccess).engine(engine)
            .allowAllAccess(true).build();
        context.enter();
      }
      context.getPolyglotBindings().putMember(ScriptConstants.LANGUAGE_GLOBAL, "js");
      Value pythonValue = context.getBindings("js");
      Source jsSource = Source.newBuilder("js", jsScript.replaceAll("%s", text), "test.js")
          .cached(false)
          .build();
      context.eval(jsSource);
      Map<String, Object> resultMap = pythonValue.getMember("test_python_map").execute()
          .as(Map.class);
      List<Map<String, Object>> resultList = pythonValue.getMember("test_python_list").execute().as(
          List.class);
      String call_java_method = pythonValue.getMember("call_java_method").execute(new TestClass())
          .asString();
    } finally {
      if (!oneContext) {
        context.close();
      }
    }
    return "ok";
  }

  @GetMapping("/test/mix/{text}")
  public String testMix(@PathVariable("text") String text,
      @RequestParam(value = "oneContext") boolean oneContext) throws IOException {
    if (oneContext) {
      try (Context context = Context.newBuilder().allowHostAccess(hostAccess).engine(engine)
          .allowAllAccess(true).build()) {
        context.enter();
        testJs(text, oneContext);
        testPython(text, oneContext);
      }
    } else {
      testJs(text, oneContext);
      testPython(text, oneContext);
    }
    return "ok";
  }

  public class TestClass {

    public String getString() {
      return "fromJava";
    }
  }
}
