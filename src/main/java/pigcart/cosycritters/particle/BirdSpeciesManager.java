package pigcart.cosycritters.particle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.RandomSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BirdSpeciesManager extends SimplePreparableReloadListener<Map<Identifier, BirdSpecies>> {
    private static final Gson GSON = new GsonBuilder().create();
    private static Map<Identifier, BirdSpecies> SPECIES = Collections.emptyMap();

    @Override
    protected Map<Identifier, BirdSpecies> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, BirdSpecies> map = new HashMap<>();
        
        // Moderne listResources API: verwacht een String (root) en een Predicate<Identifier> (filter)
        resourceManager.listResources("birds", id -> id.getNamespace().equals("worldalive") && id.getPath().endsWith(".json"))
            .forEach((id, resource) -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open()))) {
                    BirdSpecies species = GSON.fromJson(reader, BirdSpecies.class);
                    if (species != null) {
                        species.id = id;
                        map.put(id, species);
                    }
                } catch (Exception e) {
                    // Foutafhandeling
                }
            });
        
        return map;
    }

    @Override
    protected void apply(Map<Identifier, BirdSpecies> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        SPECIES = object;
    }

    public static Collection<BirdSpecies> getAll() {
        return SPECIES.values();
    }

    public static List<BirdSpecies> getAllSorted() {
        List<BirdSpecies> all = new ArrayList<>(getAll());
        all.sort(Comparator.comparing(s -> s.id.toString()));
        return all;
    }

    public static Optional<BirdSpecies> pickAny(RandomSource random) {
        List<BirdSpecies> list = getAllSorted();
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(random.nextInt(list.size())));
    }
}