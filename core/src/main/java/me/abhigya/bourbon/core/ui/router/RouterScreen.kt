package me.abhigya.bourbon.core.ui.router

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.renderCurrentDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.single
import me.abhigya.bourbon.core.ui.AppScreen
import me.abhigya.bourbon.core.ui.ar.ArScreen
import me.abhigya.bourbon.core.ui.auth.AuthScreen
import me.abhigya.bourbon.core.ui.caloriecalc.CalorieViewerScreen
import me.abhigya.bourbon.core.ui.exercises.ExerciseListScreen
import me.abhigya.bourbon.core.ui.home.HomeScreen
import me.abhigya.bourbon.core.ui.onboarding.OnBoardingScreen
import me.abhigya.bourbon.core.ui.recipe.MakeSomethingOutOfScreen
import me.abhigya.bourbon.core.ui.splash.SplashAfterOnboardScreen
import me.abhigya.bourbon.core.ui.splash.SplashHomeViewModel
import me.abhigya.bourbon.domain.UserRepository
import me.abhigya.bourbon.domain.entities.Burpee
import me.abhigya.bourbon.domain.entities.Models
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

object RouterScreen : AppScreen {

    private val excludedDirections = listOf(
        RoutePath.SPLASH_AFTER_ONBOARDING.directions().build(),
        RoutePath.ONBOARDING.directions().build(),
    )

    @Composable
    override operator fun invoke() {
        val coroutine = rememberCoroutineScope()
        val viewModel: RouterViewModel = remember(coroutine) { get { parametersOf(coroutine) } }
        val routerState: Backstack<RoutePath> by viewModel.observeStates().collectAsState()

        CompositionLocalProvider(LocalRouter provides viewModel) {
            routerState.renderCurrentDestination(
                route = {
                    AnimatedContent(
                        targetState = it,
                        label = "Page",
                        transitionSpec = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300, easing = EaseIn)
                            ).togetherWith(
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = EaseOut)
                                )
                            )
                        }
                    ) { targetState ->
                        when (targetState) {
                            RoutePath.SPLASH_AFTER_ONBOARDING -> SplashAfterOnboardScreen()
                            RoutePath.HOME -> HomeScreen()
                            RoutePath.AUTH -> AuthScreen()
                            RoutePath.ONBOARDING -> OnBoardingScreen()
                            RoutePath.AR_SCENE -> ArScreen(Models.Burpee)()
                            RoutePath.EXERCISE_LIST -> ExerciseListScreen()
                            RoutePath.CALORIE_VIEWER -> CalorieViewerScreen()
                            RoutePath.MAKE_SOMETHING_OUT_OF -> MakeSomethingOutOfScreen()
                        }
                    }
                },
                notFound = { }
            )

            BackHandler(routerState.size > 1 && routerState.last().originalDestinationUrl !in excludedDirections) {
                viewModel.trySend(RouterContract.Inputs.GoBack())
            }
        }


        LaunchedEffect(coroutine) {
            val user = get<UserRepository>()
            delay(50)
            if (!user.isLoggedIn().single()) {
                viewModel.trySend(RouterContract.Inputs.ReplaceTopDestination(RoutePath.AUTH.directions().build()))
            } else if (user.hasData(user.currentUser().single()).single()) {
                user.loadUserFully()
            } else {
                viewModel.trySend(RouterContract.Inputs.ReplaceTopDestination(RoutePath.ONBOARDING.directions().build()))
            }

            SplashHomeViewModel.finish()
        }
    }
}

val LocalRouter = staticCompositionLocalOf<RouterViewModel> {
    error("Router not provided")
}