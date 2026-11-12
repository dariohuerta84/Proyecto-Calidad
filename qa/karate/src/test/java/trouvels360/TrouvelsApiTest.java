package trouvels360;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QA-01 — Ejecuta todas las features en paralelo y genera target/karate-reports/karate-summary.html.
 *
 *   mvn test                              (entorno local, http://localhost:8000/api)
 *   mvn test -Dkarate.env=ci              (usa la variable de entorno API_URL)
 *   mvn test -Dkarate.options="--tags @HU-V07"   (solo una historia)
 */
class TrouvelsApiTest {

    @Test
    void suiteCompleta() {
        Results r = Runner.path("classpath:trouvels360")
                .tags("~@ignore")
                .outputCucumberJson(true)
                .outputJunitXml(true)
                .parallel(Integer.getInteger("hilos", 4));
        assertEquals(0, r.getFailCount(), r.getErrorMessages());
    }
}
