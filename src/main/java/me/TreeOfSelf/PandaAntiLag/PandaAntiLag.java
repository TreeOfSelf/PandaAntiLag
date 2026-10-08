package me.TreeOfSelf.PandaAntiLag;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaAntiLag implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("panda-anti-lag");

	@Override
	public void onInitialize() {
		LOGGER.info("PandaAntiLag Started!");
		AntiLagSettings.loadConfig();
		EnderPearlLimiter.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PandaAntiLagCommand.register(dispatcher));
	}
}
