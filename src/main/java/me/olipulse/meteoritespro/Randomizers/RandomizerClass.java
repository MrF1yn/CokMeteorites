// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Randomizers;

import me.olipulse.meteoritespro.Meteorites.Meteorite;
import me.olipulse.meteoritespro.Particles.MeteoriteParticle;
import me.olipulse.meteoritespro.Guardians.TreasureGuardian;
import java.util.Iterator;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import org.bukkit.Material;
import java.util.Objects;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class RandomizerClass
{
    private List<Chance> chances;
    private int sum;
    private Random random;
    
    public RandomizerClass(final long seed) {
        this.random = new Random(seed);
        this.chances = new ArrayList<Chance>();
        this.sum = 0;
    }
    
    public boolean addMaterials(final ConfigurationSection configurationSection, final MeteoritesPro plugin) {
        try {
            for (final String materialString : Objects.requireNonNull(configurationSection).getKeys(false)) {
                final Material material = Material.getMaterial(materialString);
                if (material == null) {
                    throw new ConfigException("Invalid material type: '" + materialString + "'");
                }
                this.addMaterialChance(material, configurationSection.getInt(materialString));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
    
    public void addGuardians(final MeteoritesPro plugin, final ConfigurationSection configurationSection) throws ConfigException {
        for (final String guardianString : Objects.requireNonNull(configurationSection).getKeys(false)) {
            if (configurationSection.getBoolean(guardianString + ".enabled")) {
                final TreasureGuardian guardian = new TreasureGuardian(plugin, guardianString);
                this.addGuardianChance(guardian, configurationSection.getInt(guardianString + ".chance"));
                guardian.setGuardianMaterials();
                guardian.setGuardianAttributeValues();
                guardian.setGuardianSpawnSound();
            }
        }
        if (this.chances.isEmpty()) {
            throw new ConfigException("All guardians are individually disabled but 'enable-treasure-guardian' was set to true, please set 'enable-treasure-guardian' to false to avoid errors!");
        }
    }
    
    public void addParticles(final MeteoritesPro plugin, final ConfigurationSection configurationSection) throws ConfigException {
        for (final String particleString : configurationSection.getKeys(false)) {
            if (configurationSection.getBoolean(particleString + ".enabled")) {
                final MeteoriteParticle particle = new MeteoriteParticle(plugin, particleString);
                this.addParticleChance(particle, configurationSection.getInt(particleString + ".chance"));
            }
        }
        if (this.chances.isEmpty()) {
            throw new ConfigException("All particles are individually disabled but 'enable-meteorite-particles' was set to true, please set 'enable-meteorite-particles' to false to avoid errors!");
        }
    }
    
    public Material getRandomMaterial() {
        final int index = this.random.nextInt(this.sum);
        for (final Chance chance : this.chances) {
            if (chance.getLowerLimit() <= index && chance.getUpperLimit() > index) {
                return chance.getMaterial();
            }
        }
        return null;
    }
    
    public TreasureGuardian getRandomGuardian() {
        final int index = this.random.nextInt(this.sum);
        for (final Chance chance : this.chances) {
            if (chance.getLowerLimit() <= index && chance.getUpperLimit() > index) {
                return chance.getGuardian();
            }
        }
        return null;
    }
    
    public MeteoriteParticle getRandomParticle() {
        final int index = this.random.nextInt(this.sum);
        for (final Chance chance : this.chances) {
            if (chance.getLowerLimit() <= index && chance.getUpperLimit() > index) {
                return chance.getParticle();
            }
        }
        return null;
    }
    
    public Meteorite getRandomMeteorite() {
        final int index = this.random.nextInt(this.sum);
        for (final Chance chance : this.chances) {
            if (chance.getLowerLimit() <= index && chance.getUpperLimit() > index) {
                return chance.getMeteorite();
            }
        }
        return null;
    }
    
    public void addMaterialChance(final Material material, final int chance) {
        if (!this.chances.contains(material)) {
            this.chances.add(new Chance(material, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }
    
    public void addGuardianChance(final TreasureGuardian guardian, final int chance) {
        if (!this.chances.contains(guardian)) {
            this.chances.add(new Chance(guardian, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }
    
    public void addParticleChance(final MeteoriteParticle particle, final int chance) {
        if (!this.chances.contains(particle)) {
            this.chances.add(new Chance(particle, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }
    
    public void addMeteoriteChance(final Meteorite meteorite, final int chance) {
        if (!this.chances.contains(meteorite)) {
            this.chances.add(new Chance(meteorite, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }
    
    private class Chance
    {
        private int upperLimit;
        private int lowerLimit;
        private Material material;
        private TreasureGuardian guardian;
        private MeteoriteParticle particle;
        private Meteorite meteorite;
        
        public Chance(final Material material, final int lowerLimit, final int upperLimit) {
            this.material = material;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }
        
        public Chance(final TreasureGuardian guardian, final int lowerLimit, final int upperLimit) {
            this.guardian = guardian;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }
        
        public Chance(final MeteoriteParticle particle, final int lowerLimit, final int upperLimit) {
            this.particle = particle;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }
        
        public Chance(final Meteorite meteorite, final int lowerLimit, final int upperLimit) {
            this.meteorite = meteorite;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }
        
        public int getUpperLimit() {
            return this.upperLimit;
        }
        
        public int getLowerLimit() {
            return this.lowerLimit;
        }
        
        public Material getMaterial() {
            return this.material;
        }
        
        public TreasureGuardian getGuardian() {
            return this.guardian;
        }
        
        public MeteoriteParticle getParticle() {
            return this.particle;
        }
        
        public Meteorite getMeteorite() {
            return this.meteorite;
        }
        
        @Override
        public String toString() {
            if (this.material != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.material.toString();
            }
            if (this.guardian != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.guardian.toString();
            }
            if (this.particle != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.particle.toString();
            }
            return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.meteorite.toString();
        }
    }
}
