<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- Przycisk skanowania -->
    <Button
        android:id="@+id/btnScan"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Skanuj QR"
        android:textSize="18sp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Etykieta numeru sekwencji -->
    <TextView
        android:id="@+id/tvSequenceHeader"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Numer sekwencji:"
        android:textStyle="bold"
        android:textSize="18sp"
        android:layout_marginTop="24dp"
        app:layout_constraintTop_toBottomOf="@id/btnScan"
        app:layout_constraintStart_toStartOf="parent" />

    <!-- Wartość numeru sekwencji (większa czcionka) -->
    <TextView
        android:id="@+id/tvSequence"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="-"
        android:textStyle="bold"
        android:textSize="28sp"
        app:layout_constraintTop_toBottomOf="@id/tvSequenceHeader"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Nagłówek listy wyników: WYCIĘCIA: (większa czcionka) -->
    <TextView
        android:id="@+id/tvVariantsHeader"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="WYCIĘCIA:"
        android:textStyle="bold"
        android:textSize="20sp"
        android:layout_marginTop="24dp"
        app:layout_constraintTop_toBottomOf="@id/tvSequence"
        app:layout_constraintStart_toStartOf="parent" />

    <!-- Lista wyników (większa czcionka) -->
    <TextView
        android:id="@+id/tvVariants"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:text="-"
        android:textSize="18sp"
        app:layout_constraintTop_toBottomOf="@id/tvVariantsHeader"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
