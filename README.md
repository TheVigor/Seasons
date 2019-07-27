# Seasons
Сustom views for different seasons: Winter(snowflake, snowfall), Spring/Autumn (raindrop).


<img src="https://github.com/TheVigor/seasons/blob/master/gif/snow.gif" width="25%"/>   <img src="https://github.com/TheVigor/seasons/blob/master/gif/rain.gif" width="25%"/>

# Example

```xml
<com.noble.activity.seasons.spring.RainDropView
            android:id="@+id/rainDropView"
            android:background="@android:color/holo_blue_dark"
            app:rv_dot_count="50"
            app:rv_max_length="30dp"
            app:rv_min_length="10dp"
            app:rv_max_speed="30"
            app:rv_min_speed="15"
            app:rv_alpha_gradient="true"
            app:rv_max_alpha="0.6"
            app:rv_water_radius="1.7dp"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>

<com.noble.activity.seasons.winter.SnowFlakeView
            android:id="@+id/snowFlakeView"
            android:background="@android:color/holo_blue_dark"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>

```

