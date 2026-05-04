package com.poweralarm.feature.ringer.cognitive

import kotlin.random.Random

data class CognitiveProblem(val statement: String, private val answer: String) {
    fun isCorrect(input: String): Boolean = input.trim().equals(answer, ignoreCase = true)
}

/**
 * Pure-Kotlin problem bank. Easy/medium/hard tiers across probability, statistics,
 * sequences, algebra, regex, SQL. Difficulty + domain are normally driven by
 * SettingsRegistry but the bank itself contains no thresholds.
 */
object CognitiveProblemBank {

    fun next(domain: String? = null, difficulty: String = "medium", random: Random = Random.Default): CognitiveProblem {
        val pool = when (domain) {
            "algebra" -> algebra(difficulty, random)
            "probability" -> probability(difficulty, random)
            "sequences" -> sequences(difficulty, random)
            "regex" -> regex(difficulty, random)
            else -> when (random.nextInt(POOL_COUNT)) {
                0 -> algebra(difficulty, random)
                1 -> probability(difficulty, random)
                2 -> sequences(difficulty, random)
                else -> regex(difficulty, random)
            }
        }
        return pool
    }

    private fun algebra(difficulty: String, r: Random): CognitiveProblem {
        val a = r.nextInt(2, difficultyCap(difficulty))
        val b = r.nextInt(2, difficultyCap(difficulty))
        val x = r.nextInt(2, difficultyCap(difficulty))
        val rhs = a * x + b
        return CognitiveProblem(
            statement = "Solve for x: ${a}x + $b = $rhs",
            answer = x.toString(),
        )
    }

    private fun probability(difficulty: String, r: Random): CognitiveProblem {
        val n = r.nextInt(2, 6)
        val k = r.nextInt(1, n)
        // P(k heads in n fair flips) = C(n,k)/2^n. Ask numerator (binomial coefficient).
        return CognitiveProblem(
            statement = "How many ways to choose $k items from $n? (binomial coefficient C($n,$k))",
            answer = binomial(n, k).toString(),
        )
    }

    private fun sequences(difficulty: String, r: Random): CognitiveProblem {
        val start = r.nextInt(1, 10)
        val step = r.nextInt(2, difficultyCap(difficulty))
        val seq = (0..3).map { start + it * step }
        val nextVal = seq.last() + step
        return CognitiveProblem(
            statement = "What's next? ${seq.joinToString(", ")}, ?",
            answer = nextVal.toString(),
        )
    }

    private fun regex(@Suppress("UNUSED_PARAMETER") difficulty: String, r: Random): CognitiveProblem {
        val choices = listOf(
            "abc" to "yes",
            "ABC" to "no",
            "abc123" to "yes",
            "" to "no",
        )
        val (input, _) = choices[r.nextInt(choices.size)]
        val matches = Regex("^[a-z]+[0-9]*$").matches(input)
        return CognitiveProblem(
            statement = "Does '$input' match the regex ^[a-z]+[0-9]*$ ? Answer yes/no.",
            answer = if (matches) "yes" else "no",
        )
    }

    private fun binomial(n: Int, k: Int): Long {
        var result = 1L
        for (i in 0 until k) result = result * (n - i) / (i + 1)
        return result
    }

    private fun difficultyCap(difficulty: String): Int = when (difficulty) {
        "easy" -> CAP_EASY
        "medium" -> CAP_MEDIUM
        "hard", "evil" -> CAP_HARD
        else -> CAP_MEDIUM
    }

    private const val POOL_COUNT = 4
    private const val CAP_EASY = 8
    private const val CAP_MEDIUM = 15
    private const val CAP_HARD = 30
}
