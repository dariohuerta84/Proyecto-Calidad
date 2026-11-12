package com.trouvels360.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Session
import com.trouvels360.app.data.remote.UserDto
import com.trouvels360.app.ui.components.LoginRequired
import com.trouvels360.app.ui.components.appiumRoot
import com.trouvels360.app.ui.components.tag
import com.trouvels360.app.ui.screens.auth.LoginScreen
import com.trouvels360.app.ui.screens.auth.RegisterScreen
import com.trouvels360.app.ui.screens.hotels.HotelCheckoutScreen
import com.trouvels360.app.ui.screens.hotels.HotelDetailScreen
import com.trouvels360.app.ui.screens.hotels.HotelsScreen
import com.trouvels360.app.ui.screens.profile.ProfileScreen
import com.trouvels360.app.ui.screens.proveedor.HotelFormScreen
import com.trouvels360.app.ui.screens.proveedor.PanelProveedorScreen
import com.trouvels360.app.ui.screens.proveedor.ReservasRecibidasScreen
import com.trouvels360.app.ui.screens.proveedor.TourFormScreen
import com.trouvels360.app.ui.screens.proveedor.TourProgramaScreen
import com.trouvels360.app.ui.screens.reservas.MisReservasScreen
import com.trouvels360.app.ui.screens.tours.TourCheckoutScreen
import com.trouvels360.app.ui.screens.tours.TourDetailScreen
import com.trouvels360.app.ui.screens.tours.ToursScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOTELES = "hoteles"
    const val TOURS = "tours"
    const val RESERVAS = "reservas"
    const val CUENTA = "cuenta"
    const val LOGIN = "login?expirada={expirada}"
    const val REGISTRO = "registro"
    const val HOTEL = "hotel/{id}"
    const val TOUR = "tour/{id}"
    const val PAGO_HOTEL = "pago_hotel"
    const val PAGO_TOUR = "pago_tour"
    // Proveedor
    const val PANEL = "panel"
    const val RECIBIDAS = "recibidas?servicio={servicio}&estado={estado}"
    const val HOTEL_NUEVO = "prov_hotel_nuevo"
    const val HOTEL_EDITAR = "prov_hotel/{id}"
    const val TOUR_NUEVO = "prov_tour_nuevo"
    const val TOUR_EDITAR = "prov_tour/{id}"
    const val PROGRAMA = "prov_programa/{id}"

    fun login(expirada: Boolean = false) = "login?expirada=$expirada"
    fun hotel(id: Int) = "hotel/$id"
    fun tour(id: Int) = "tour/$id"
    fun recibidas(servicio: Int? = null, estado: String? = null) = "recibidas?servicio=${servicio ?: -1}&estado=${estado ?: ""}"
    fun hotelEditar(id: Int) = "prov_hotel/$id"
    fun tourEditar(id: Int) = "prov_tour/$id"
    fun programa(id: Int) = "prov_programa/$id"
}

private data class Tab(val route: String, val navRoute: String, val label: String, val icon: ImageVector, val tagId: String)

private val tabsViajero = listOf(
    Tab(Routes.HOTELES, Routes.HOTELES, "Hoteles", Icons.Filled.Bed, "nav_hoteles"),
    Tab(Routes.TOURS, Routes.TOURS, "Tours", Icons.Filled.Hiking, "nav_tours"),
    Tab(Routes.RESERVAS, Routes.RESERVAS, "Reservas", Icons.Filled.EventAvailable, "nav_reservas"),
    Tab(Routes.CUENTA, Routes.CUENTA, "Cuenta", Icons.Filled.Person, "nav_cuenta"),
)

private val tabsProveedor = listOf(
    Tab(Routes.PANEL, Routes.PANEL, "Panel", Icons.Filled.Dashboard, "nav_panel"),
    Tab(Routes.RECIBIDAS, Routes.recibidas(), "Recibidas", Icons.Filled.Inbox, "nav_recibidas"),
    Tab(Routes.CUENTA, Routes.CUENTA, "Cuenta", Icons.Filled.Person, "nav_cuenta"),
)

private fun titleFor(route: String?) = when (route) {
    Routes.HOTELES -> "Trouvels360"
    Routes.TOURS -> "Tours"
    Routes.RESERVAS -> "Mis reservas"
    Routes.CUENTA -> "Mi cuenta"
    Routes.LOGIN -> "Iniciar sesión"
    Routes.REGISTRO -> "Registrarse"
    Routes.HOTEL -> "Detalle del hotel"
    Routes.TOUR -> "Detalle del tour"
    Routes.PAGO_HOTEL, Routes.PAGO_TOUR -> "Pago"
    Routes.PANEL -> "Mis publicaciones"
    Routes.RECIBIDAS -> "Reservas recibidas"
    Routes.HOTEL_NUEVO, Routes.HOTEL_EDITAR -> "Hotel"
    Routes.TOUR_NUEVO, Routes.TOUR_EDITAR -> "Tour"
    Routes.PROGRAMA -> "Salidas y actividades"
    else -> "Trouvels360"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(nav: NavHostController = rememberNavController()) {
    val user by Session.user.collectAsState()
    val expirada by Session.expirada.collectAsState()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = if (user?.esProveedor == true) tabsProveedor else tabsViajero
    val isTab = tabs.any { it.route == route }
    val scope = rememberCoroutineScope()

    fun irAInicio(u: UserDto?) {
        val destino = if (u?.esProveedor == true) Routes.PANEL else Routes.HOTELES
        nav.navigate(destino) { popUpTo(nav.graph.id) { inclusive = true }; launchSingleTop = true }
    }

    // Con sesión de proveedor guardada, la app abre en su panel.
    LaunchedEffect(user?.rol) {
        if (user?.esProveedor == true && nav.currentDestination?.route == Routes.HOTELES) irAInicio(user)
    }

    // Sesión expirada (401 sin refresco posible): ir al login con aviso (HU-V02)
    LaunchedEffect(expirada) {
        if (expirada) {
            nav.navigate(Routes.login(true)) { launchSingleTop = true }
            Session.avisoExpiracionVisto()
        }
    }

    Scaffold(
        modifier = Modifier.appiumRoot(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(route), fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("titulo_pantalla")) },
                navigationIcon = {
                    if (!isTab && nav.previousBackStackEntry != null) {
                        IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.tag("btn_atras")) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    }
                },
            )
        },
        bottomBar = {
            if (isTab) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = entry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.navRoute) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) },
                            modifier = Modifier.tag(tab.tagId),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.HOTELES, modifier = Modifier.padding(padding)) {
            // ---------------------------------------------------------------- Viajero / público
            composable(Routes.HOTELES) { HotelsScreen(onOpen = { nav.navigate(Routes.hotel(it)) }) }
            composable(Routes.TOURS) { ToursScreen(onOpen = { nav.navigate(Routes.tour(it)) }) }
            composable(Routes.HOTEL, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                HotelDetailScreen(
                    id = e.arguments?.getInt("id") ?: 0,
                    onLogin = { nav.navigate(Routes.login()) },
                    onCheckout = { nav.navigate(Routes.PAGO_HOTEL) },
                    onVerTour = { nav.navigate(Routes.tour(it)) },
                )
            }
            composable(Routes.TOUR, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                TourDetailScreen(
                    id = e.arguments?.getInt("id") ?: 0,
                    onLogin = { nav.navigate(Routes.login()) },
                    onCheckout = { nav.navigate(Routes.PAGO_TOUR) },
                )
            }
            composable(Routes.PAGO_HOTEL) {
                if (user?.esViajero != true) LoginRequired("Inicia sesión como viajero para pagar.") { nav.navigate(Routes.login()) }
                else HotelCheckoutScreen(
                    onMisReservas = { nav.navigate(Routes.RESERVAS) { popUpTo(Routes.HOTELES) } },
                    onVolver = { nav.popBackStack() },
                )
            }
            composable(Routes.PAGO_TOUR) {
                if (user?.esViajero != true) LoginRequired("Inicia sesión como viajero para pagar.") { nav.navigate(Routes.login()) }
                else TourCheckoutScreen(
                    onMisReservas = { nav.navigate(Routes.RESERVAS) { popUpTo(Routes.HOTELES) } },
                    onVolver = { nav.popBackStack() },
                )
            }
            composable(Routes.RESERVAS) {
                val u = user
                when {
                    u == null -> LoginRequired("Inicia sesión para ver tus reservas.") { nav.navigate(Routes.login()) }
                    !u.esViajero -> LoginRequired("Las reservas son para cuentas de viajero.", "Ir al panel") { irAInicio(u) }
                    else -> MisReservasScreen()
                }
            }
            composable(Routes.CUENTA) {
                val u = user
                if (u == null) LoginRequired("Inicia sesión o crea una cuenta para reservar o publicar.") { nav.navigate(Routes.login()) }
                else ProfileScreen(
                    user = u,
                    onLogout = { scope.launch { Repository.logout(); irAInicio(null) } },
                    onCuentaEliminada = { irAInicio(null) },
                )
            }

            // ---------------------------------------------------------------- Auth
            composable(Routes.LOGIN, arguments = listOf(navArgument("expirada") { type = NavType.BoolType; defaultValue = false })) { e ->
                LoginScreen(
                    sesionExpirada = e.arguments?.getBoolean("expirada") ?: false,
                    // Retorno al destino: el viajero vuelve a la pantalla que pidió el login; el proveedor va a su panel.
                    onDone = { u -> if (u?.esProveedor == true) irAInicio(u) else if (!nav.popBackStack()) irAInicio(u) },
                    onRegister = { nav.navigate(Routes.REGISTRO) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                )
            }
            composable(Routes.REGISTRO) {
                RegisterScreen(
                    onDone = { u -> irAInicio(u) },
                    onLogin = { nav.navigate(Routes.login()) { popUpTo(Routes.REGISTRO) { inclusive = true } } },
                )
            }

            // ---------------------------------------------------------------- Proveedor
            composable(Routes.PANEL) {
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) {
                    PanelProveedorScreen(
                        onEditarHotel = { nav.navigate(Routes.hotelEditar(it)) },
                        onEditarTour = { nav.navigate(Routes.tourEditar(it)) },
                        onPrograma = { nav.navigate(Routes.programa(it)) },
                        onReservas = { servicio, estado -> nav.navigate(Routes.recibidas(servicio, estado)) },
                        onCrearHotel = { nav.navigate(Routes.HOTEL_NUEVO) },
                        onCrearTour = { nav.navigate(Routes.TOUR_NUEVO) },
                    )
                }
            }
            composable(
                Routes.RECIBIDAS,
                arguments = listOf(
                    navArgument("servicio") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("estado") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { e ->
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) {
                    val servicio = e.arguments?.getInt("servicio")?.takeIf { it > 0 }
                    val estado = e.arguments?.getString("estado")?.takeIf { it.isNotBlank() }
                    ReservasRecibidasScreen(servicio, estado)
                }
            }
            composable(Routes.HOTEL_NUEVO) {
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) { HotelFormScreen(null, onCreado = { nav.popBackStack() }) }
            }
            composable(Routes.HOTEL_EDITAR, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) { HotelFormScreen(e.arguments?.getInt("id"), onCreado = { nav.popBackStack() }) }
            }
            composable(Routes.TOUR_NUEVO) {
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) {
                    TourFormScreen(null, onCreado = { nav.popBackStack() }, onPrograma = { nav.navigate(Routes.programa(it)) })
                }
            }
            composable(Routes.TOUR_EDITAR, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) {
                    TourFormScreen(e.arguments?.getInt("id"), onCreado = { nav.popBackStack() }, onPrograma = { nav.navigate(Routes.programa(it)) })
                }
            }
            composable(Routes.PROGRAMA, arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                ProveedorGuard(user, onLogin = { nav.navigate(Routes.login()) }) { TourProgramaScreen(e.arguments?.getInt("id") ?: 0) }
            }
        }
    }
}

/** Equivalente al proveedorGuard de la web: solo cuentas de proveedor. */
@Composable
private fun ProveedorGuard(user: UserDto?, onLogin: () -> Unit, content: @Composable () -> Unit) {
    when {
        user == null -> LoginRequired("Inicia sesión con tu cuenta de proveedor.", onLogin = onLogin)
        !user.esProveedor -> LoginRequired("Esta sección es solo para proveedores.", "Iniciar con otra cuenta", onLogin)
        else -> content()
    }
}
