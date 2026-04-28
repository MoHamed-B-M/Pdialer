@Composable
fun NewSwipeToAnswer(onAnswer: () -> Unit, onDecline: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = LocalView.current
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // ── Staggered ripple rings ──
    val infiniteTransition = rememberInfiniteTransition(label = "swipeRings")
    val ring1 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(
            tween(3000, easing = LinearOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(
            tween(3000, delayMillis = 1000, easing = LinearOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring3 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(
            tween(3000, delayMillis = 2000, easing = LinearOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "ring3"
    )

    // ── Hint text breathing pulse ──
    val hintAlpha by infiniteTransition.animateFloat(
        0.3f, 0.8f,
        infiniteRepeatable(
            tween(2500, easing = EaseInOutCubic),
            RepeatMode.Reverse
        ),
        label = "hintPulse"
    )

    // ── Thumb scale on grab ──
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "thumbScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Message Button ──
        Surface(
            onClick = { /* Handle message */ },
            shape = CircleShape,
            color = Color.White.copy(0.1f),
            modifier = Modifier
                .height(45.dp)
                .width(140.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    null,
                    tint = Color.White.copy(0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Message",
                    color = Color.White.copy(0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // ── Swipe Track ──
        BoxWithConstraints(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(0.88f)
                .clip(CircleShape)
                .background(Color.White.copy(0.06f)),
            contentAlignment = Alignment.Center
        ) {
            val halfTrackPx = maxWidth / 2
            val thumbHalf = with(density) { 34.dp.toPx() }
            val edgePad = with(density) { 8.dp.toPx() }
            val maxDragPx = halfTrackPx - thumbHalf - edgePad

            val currentProgress by remember {
                derivedStateOf {
                    (kotlin.math.abs(offsetX.value) / maxDragPx).coerceIn(0f, 1f)
                }
            }
            val swipingRight = offsetX.value > 0

            // ── Decline gradient fill (left) ──
            if (!swipingRight && currentProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(currentProgress * 0.5f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF5449).copy(alpha = 0.45f),
                                    Color(0xFFFF5449).copy(alpha = 0.02f)
                                )
                            )
                        )
                )
            }

            // ── Answer gradient fill (right) ──
            if (swipingRight && currentProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .fillMaxWidth(currentProgress * 0.5f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E676).copy(alpha = 0.02f),
                                    Color(0xFF00E676).copy(alpha = 0.45f)
                                )
                            )
                        )
                )
            }

            // ── Edge Labels ──
            val declineActive = !swipingRight && currentProgress > 0.15f
            val answerActive = swipingRight && currentProgress > 0.15f

            Text(
                text = "Decline",
                color = Color(0xFFFF5449).copy(alpha = if (declineActive) 1f else 0.4f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (declineActive) FontWeight.Bold else FontWeight.Medium
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 28.dp)
                    .graphicsLayer {
                        scaleX = if (declineActive) 1.1f else 1f
                        scaleY = if (declineActive) 1.1f else 1f
                    }
            )
            Text(
                text = "Answer",
                color = Color(0xFF00E676).copy(alpha = if (answerActive) 1f else 0.4f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (answerActive) FontWeight.Bold else FontWeight.Medium
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp)
                    .graphicsLayer {
                        scaleX = if (answerActive) 1.1f else 1f
                        scaleY = if (answerActive) 1.1f else 1f
                    }
            )

            // ── Ripple rings (behind thumb, hidden while dragging) ──
            if (!isDragging) {
                listOf(ring1, ring2, ring3).forEach { ringProgress ->
                    // FIX: Calculate size as Float first, then cast to Dp
                    val ringSizeDp = (68f + ringProgress * 48f).dp
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(ringSizeDp)
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF00E676).copy(
                                    alpha = 0.22f * (1f - ringProgress)
                                ),
                                shape = CircleShape
                            )
                            .alpha(1f - ringProgress)
                    )
                }
            }

            // ── Draggable Thumb ──
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .size(68.dp)
                    .scale(thumbScale)
                    .pointerInput(maxDragPx) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                view.performHapticFeedback(
                                    HapticFeedbackConstants.VIRTUAL_KEY
                                )
                            },
                            onDragEnd = {
                                isDragging = false
                                coroutineScope.launch {
                                    when {
                                        offsetX.value > maxDragPx * 0.5f -> {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstants.CONFIRM
                                            )
                                            offsetX.animateTo(
                                                maxDragPx,
                                                tween(220, easing = FastOutSlowInEasing)
                                            )
                                            onAnswer()
                                        }
                                        offsetX.value < -maxDragPx * 0.5f -> {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstants.CONFIRM
                                            )
                                            offsetX.animateTo(
                                                -maxDragPx,
                                                tween(220, easing = FastOutSlowInEasing)
                                            )
                                            onDecline()
                                        }
                                        else -> {
                                            offsetX.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = 0.65f,
                                                    stiffness = 280f
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount)
                                            .coerceIn(-maxDragPx, maxDragPx)
                                    )
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = if (isDragging) 16.dp else 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = when {
                                swipingRight && currentProgress > 0.12f ->
                                    Color(0xFF00C853)
                                !swipingRight && currentProgress > 0.12f ->
                                    Color(0xFFFF5449)
                                else -> Color(0xFF424242)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // ── Hint text (fades out while dragging) ──
        Text(
            text = "Swipe to answer",
            color = Color.White.copy(alpha = if (isDragging) 0f else hintAlpha),
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 0.06.sp
        )
    }
}
