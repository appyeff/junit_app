package org.junit5app.ejemplos.models;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit5app.ejemplos.exceptions.DineroInsuficienteException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTestExample {

    Cuenta cuenta;

    private TestInfo testInfo;
    private TestReporter testReporter;

    @BeforeEach //antes por cada metodo
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        this.cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        System.out.println("iniciando el metodo.");
        testReporter.publishEntry("ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().get().getName()
                + " con las etiquetas " + testInfo.getTags());
    }

    @AfterEach // despues por cada metodo
    void tearDown() {
        System.out.println("Finalizando el metodo de prueba.");
    }

    @BeforeAll // una sola vez al inicio por test
    static void beforeAll() {
        System.out.println("Inicializando el test");
    }

    @AfterAll // una sola vez al final por test
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    @Tag("cuenta")
    @Test
    @DisplayName("Probando el nombre de la cuenta corriente!")
    void testCuenta() {
        testReporter.publishEntry(testInfo.getTags().toString());
        if(testInfo.getTags().contains("cuenta")){
            testReporter.publishEntry("hacer algo con la etiqueta cuenta");
        }
        cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        //cuenta.setPersona("Yeff");
        String esperado = "Yeff";
        String real = cuenta.getPersona();

        assertNotNull(real, "La cuenta no puede ser nula");
        assertEquals(esperado, real, "El nombre de la cuenta no es el que se esperaba");
        assertTrue(real.equals("Yeff"), "nombre cuenta esperada debe ser igual a la real");
    }

    @Tag("banco")
    @Tag("dinero")
    @Test
    void testSaldoCuenta() {
        cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());

        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testReferenciaCuenta() {
        cuenta = new Cuenta("Jhon", new BigDecimal("8900.9997"));
        Cuenta cuenta2 = new Cuenta("Jhon", new BigDecimal("8900.9997"));

        assertEquals(cuenta2, cuenta);
        //assertNotEquals(cuenta2, cuenta);
    }

    @Test
    void testDebitoCuenta() {
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());

        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testCreditoCuenta() {
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.credito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(1100, cuenta.getSaldo().intValue());

        assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testDineroInsuficienteExceptionCuenta() {
        cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> cuenta.debito(new BigDecimal(1500)));
        String actual = exception.getMessage();
        String esperado = "Dinero Insuficiente";
        assertEquals(esperado, actual);
    }

    @Test
    void testTransferirDineroCuentas() {
        Cuenta cuenta1 = new Cuenta("Jhon", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.setNombre("Banco del estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
        assertEquals("3000", cuenta1.getSaldo().toPlainString());
    }

    @Test
    void testRelacionesBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("Jhon", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Yeff", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

        assertAll(() -> assertEquals("1000.8989", cuenta2.getSaldo().toPlainString()),
                () -> assertEquals("3000", cuenta1.getSaldo().toPlainString()),
                () -> assertEquals(2, banco.getCuentas().size()));

        assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());
        assertEquals("Yeff", banco.getCuentas().stream()
                .filter(c -> c.getPersona().equals("Yeff"))
                .findFirst()
                .get().getPersona());

        assertTrue(banco.getCuentas().stream()
                .filter(c -> c.getPersona().equals("Yeff"))
                .findFirst().isPresent());

        assertTrue(banco.getCuentas().stream()
                .anyMatch(c -> c.getPersona().equals("Jhon")));
    }

    @Tag("Param")
    @Nested
    class SistemaOperativoTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows(){
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac(){
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @DisabledOnJre(JRE.JAVA_8)
        void testSoloJdk8() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testSoloJdk15() {
        }
    }

    @Test
    void imprimirSystemProperties(){
        Properties properties = System.getProperties();
        properties.forEach((k, v)-> System.out.println(k + ":" + v));
    }

    @Test
    @EnabledIfSystemProperty(named= "java.version", matches = "11.0.10")
    void testJavaVersion(){
    }

    @Test
    @EnabledIfSystemProperty(named= "user.name", matches = "yefer")
    void testUsername(){
    }

    @Test
    @EnabledIfSystemProperty(named= "ENV", matches = "dev")
    void testDev(){
    }

    @Test
    void imprimirVariablesAmbiente() {
        Map<String, String> getenv = System.getenv();
        getenv.forEach((k, v) -> System.out.println(k + " = " + v));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-15.0.1.*")
    void testJavaHome(){
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "12")
    void testProcesadores(){
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
    void testEnvProdDisabled() {
    }

    @Test
    @DisplayName("probando el saldo de la cuenta corriente, que no sea null, mayor que cero, valor esperado.")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());

        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @DisplayName("Probando Debito Cuenta Repetir!")
    @RepeatedTest(value=5, name= "Repetición numero {currentRepetition} de {totalRepetition}")
    void testCuentaRepetead(RepetitionInfo info) {
        if(info.getCurrentRepetition() == 3) {
            System.out.println("estamos en la repeticion " + info.getCurrentRepetition());
        }
        cuenta = new Cuenta("Yeff", new BigDecimal("1000.12345"));
        String esperado = "Yeff";
        String real = cuenta.getPersona();

        assertNotNull(real, "La cuenta no puede ser nula");
        assertEquals(esperado, real, "El nombre de la cuenta no es el que se esperaba");
        assertTrue(real.equals("Yeff"), "nombre cuenta esperada debe ser igual a la real");
    }

    @ParameterizedTest(name = "numero  {index} ejecutando con valor {0} - {argumentsWithNames}")
    @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
    void testDebitoCuentaParameterized(String monto) {
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero  {index} ejecutando con valor {0} - {argumentsWithNames}")
    @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
    void testDebitoCuentaParameterizedCsv(String index, String monto) {
        System.out.println(index + " -> " + monto);
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero  {index} ejecutando con valor {0} - {argumentsWithNames}")
    @CsvSource({"200,100,Tito,Tito", "250,200,Renato,Renato", "300,299,Pepe,Pepe", "510,500,maria,maria", "750,700,Pepa,Pepa", "1010,1000,Luca,Luca"})
    void testDebitoCuentaParameterizedCsv2(String saldo, String monto, String esperado, String actual) {
        System.out.println(saldo + " -> " + monto);
        cuenta.setSaldo(new BigDecimal(saldo));
        cuenta.debito(new BigDecimal(monto));
        cuenta.setPersona(actual);

        assertNotNull(cuenta.getSaldo());
        assertNotNull(cuenta.getPersona());
        assertEquals(esperado, actual);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero  {index} ejecutando con valor {0} - {argumentsWithNames}")
    @CsvFileSource(resources = "/data.csv")
    void testDebitoCuentaParameterizedCsvSource(String monto) {
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero  {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaParameterizedMethodSource(String monto) {
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList(){
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeOutTest {
        @Test
        @Timeout(7)
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.SECONDS.sleep(6);
        }

        @Test
        @Timeout(value=1000, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeOutAssertions(){
            assertTimeout(Duration.ofSeconds(5), ()-> {
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }
}