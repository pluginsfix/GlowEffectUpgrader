package pluginsfix.gloweffectupgrader.domain;

public sealed interface UpgradeResult {
    record Success(AppliedEffect effect, boolean byMoney, double priceMoney, int pricePoints) implements UpgradeResult {}
    record AlreadyHasEffect() implements UpgradeResult {}
    record NotEnoughMoney(double required, double current) implements UpgradeResult {}
    record NotEnoughPoints(int required, int current) implements UpgradeResult {}
    record EconomyUnavailable() implements UpgradeResult {}
    record PointsUnavailable() implements UpgradeResult {}
    record InvalidItem() implements UpgradeResult {}
}
