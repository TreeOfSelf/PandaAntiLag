package me.sebastian420.PandaAntiLag;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaAntiLag implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("panda-anti-lag");

	@Override
	public void onInitialize() {
		LOGGER.info("PandaAntiLag Started!");
	}
}