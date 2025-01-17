package me.abhigya.bourbon.core.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copperleaf.ballast.navigation.routing.RouterContract
import me.abhigya.bourbon.core.ui.AppScreen
import me.abhigya.bourbon.core.ui.components.AppBar
import me.abhigya.bourbon.core.ui.components.BackButton
import me.abhigya.bourbon.core.ui.router.LocalRouter
import me.abhigya.bourbon.core.utils.bouncyClick
import me.abhigya.bourbon.core.utils.navigationBarsPadding
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

private val STEP_COUPLE = listOf(
    listOf(OnBoardingContract.Step.Weight),
    listOf(OnBoardingContract.Step.Height),
    listOf(OnBoardingContract.Step.GenderAndAge),
    listOf(OnBoardingContract.Step.BmiScale),
    listOf(OnBoardingContract.Step.GoalAndAim, OnBoardingContract.Step.Training, OnBoardingContract.Step.ActivityLevel),
    listOf(OnBoardingContract.Step.Diet, OnBoardingContract.Step.MealFrequency),
    listOf(OnBoardingContract.Step.FetchData)
)

object OnBoardingScreen : AppScreen {

    @Composable
    override operator fun invoke() {
        val router = LocalRouter.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel: OnBoardingViewModel = remember(coroutineScope) { get { parametersOf(coroutineScope, router) } }
        val uiState = viewModel.observeStates().collectAsState()
        var currPage by remember { mutableIntStateOf(0) }

        BackHandler {
            if (uiState.value.step == OnBoardingContract.Step.FetchData) return@BackHandler
            if (currPage > 0) {
                currPage--
            } else {
                router.trySend(RouterContract.Inputs.GoBack())
            }
        }

        Scaffold(
            topBar = {
                if (uiState.value.step == OnBoardingContract.Step.FetchData) return@Scaffold
                AnimatedVisibility(
                    visible = currPage > 0
                ) {
                    AppBar {
                        BackButton {
                            if (currPage > 0) {
                                currPage--
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (uiState.value.step == OnBoardingContract.Step.FetchData) return@Scaffold
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(336.dp)
                            .height(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .bouncyClick()
                            .clickable {
                                val page =
                                    STEP_COUPLE.indexOfFirst { it.contains(uiState.value.step) }
                                val isLast = STEP_COUPLE[page].last() == uiState.value.step
                                if (currPage < page) {
                                    currPage++
                                    return@clickable
                                }
                                if (isLast && page < STEP_COUPLE.lastIndex) {
                                    currPage++
                                }
                                viewModel.trySend(OnBoardingContract.Inputs.NextButton)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = currPage,
                label = "Onboarding Page",
                transitionSpec = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ).togetherWith(
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    )
                }
            ) { targetPage ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = targetPage == 0
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Setup",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    for (step in STEP_COUPLE[targetPage]) {
                        AnimatedVisibility(
                            visible = step <= uiState.value.step,
                            enter = slideInVertically(
                                initialOffsetY = { it * 2 },
                                animationSpec = tween(300)
                            )
                        ) {
                            getScreen(step)(viewModel, uiState)
                        }
                    }
                }
            }
        }
    }

    private fun getScreen(step: OnBoardingContract.Step): StepScreen = when (step) {
        OnBoardingContract.Step.Weight -> WeightStepScreen
        OnBoardingContract.Step.Height -> HeightStepScreen
        OnBoardingContract.Step.GenderAndAge -> GenderAndAgeStepScreen
        OnBoardingContract.Step.BmiScale -> BmiScaleStepScreen
        OnBoardingContract.Step.GoalAndAim -> GoalAndAimStepScreen
        OnBoardingContract.Step.Training -> TrainingStepScreen
        OnBoardingContract.Step.ActivityLevel -> ActivityLevelStepScreen
        OnBoardingContract.Step.Diet -> DietStepScreen
        OnBoardingContract.Step.MealFrequency -> MealFrequencyStepScreen
        OnBoardingContract.Step.FetchData -> FetchDataStepScreen
    }

}