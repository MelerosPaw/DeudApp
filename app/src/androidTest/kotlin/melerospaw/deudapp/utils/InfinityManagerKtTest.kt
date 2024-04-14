package melerospaw.deudapp.utils

import org.junit.Assert
import org.junit.Test

class InfinityManagerKtTest {

  @Test
  fun whenEnteringNegativeInfinityCharacter_thenPositiveInfinityCharacterReturnsFalse() {
    Assert.assertFalse("-∞".isPositiveInfinityCharacter())
  }
}