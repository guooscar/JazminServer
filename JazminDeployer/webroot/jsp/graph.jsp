<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Graph</title>
  </head>
  <body>
   <div id="graph"><h1 style="text-aligin:center">Loading</h1></div>
    <script type="text/vnd.graphviz" id="cluster">
	${dot_string}
	</script>
    <script src="/js/viz.js"></script>
    <script src="/js/jquery.min.js"></script>
    
    <script>

      function inspect(s) {
        return "<pre>" + s.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\"/g, "&quot;") + "</pre>"
      }
      
      function src(id) {
        return document.getElementById(id).innerHTML;
      }
      
      function example(id, format, engine) {
        var result;
        try {
          result = Viz(src(id), format, engine);
          if (format === "svg")
            return result;
          else
            return inspect(result);
        } catch(e) {
          return inspect(e.toString());
        }
      }
      var ss= example("cluster", "svg");
      document.getElementById("graph").innerHTML =ss;
      
    </script>
    
  </body>
</html>
