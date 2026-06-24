package fr.skylined.spectral.block.entity;

/**
 * Interface implemented by any block entity that can store and receive Photons (PH).
 * The Solar Collector (and any future PH source) discovers recipients via this interface
 * instead of hardcoded instanceof checks.
 */
public interface IPhotonAcceptor {
    long getStoredPhotons();
    long getMaxPhotons();
    void addPhotons(long amount);
}
