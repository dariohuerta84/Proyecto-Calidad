package trouvels360;

import com.intuit.karate.junit5.Karate;

public class Trouvels360Runner {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:trouvels360").relativeTo(getClass());
    }

    @Karate.Test
    Karate testProveedor() {
        return Karate.run("classpath:trouvels360/proveedor").relativeTo(getClass());
    }

    @Karate.Test
    Karate testViajero() {
        return Karate.run("classpath:trouvels360/viajero").relativeTo(getClass());
    }
}
