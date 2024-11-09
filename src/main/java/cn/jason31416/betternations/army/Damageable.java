package cn.jason31416.betternations.army;

public interface Damageable {
    double getHealth();
    void damage(double dmg);
    void damage(DamageSource source);
    boolean isAlive();
}
