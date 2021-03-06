package digester;

import settings.fimsPrinter;

import java.io.PrintWriter;

/**
 * Relation representation
 */
public class Relation {
    private String subject;
    private String predicate;
    private String object;

    public String getSubject() {
        return subject;
    }

    /**
     * Set Subject by looking up the subject from the mapping file.
     *
     * @param subject
     */
    public void addSubject(String subject) {
        //this.subject = findEntity(subject.getEntityId());
        this.subject = subject;
    }

    public void addPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public void addObject(String object) {
        //this.object = mapping.findEntity(object.getEntityId());
        this.object = object;
    }

    public void print() {
        fimsPrinter.out.println("  Relation:");
        fimsPrinter.out.println("    subject=" + subject.toString());
        fimsPrinter.out.println("    predicate=" + predicate.toString());
        fimsPrinter.out.println("    object=" + object.toString());
    }

    /**
     * Generate D2RQ Mapping Language representation of this Relation.
     *
     * @param pw     PrintWriter used to write output to.
     * @param parent Reference to parent entity (a Mapping)
     */
    public void printD2RQ(PrintWriter
                                  pw, Object parent) {
        Mapping mapping = (Mapping) parent;

        Entity subjEntity = mapping.findEntity(subject.toString());
        Entity objEntity = mapping.findEntity(object.toString());

        if (subjEntity == null || objEntity == null)
            return;

        String subjClassMap = subjEntity.classMap(),
                objClassMap = objEntity.classMap();

        //if (subjTbl.equals(objTbl)) {
        pw.println("map:" + subjClassMap + "_" + objClassMap + "_rel" + " a d2rq:PropertyBridge;");
        pw.println("\td2rq:belongsToClassMap " + "map:" + subjClassMap + ";");
        pw.println("\td2rq:property <" + predicate + ">;");
        pw.println(mapping.getPersistentIdentifier(objEntity));
        pw.println("\td2rq:condition \"" + objEntity.getWorksheetUniqueKey() + " <> ''\";");
        pw.println("\t.");
        /*} else {
            Join join = mapping.findJoin(subjTbl, objTbl);
            if (join == null)
                return;
            pw.println("map:" + subjClassMap + "_" + objClassMap + "_rel" + " a d2rq:PropertyBridge;");
            pw.println("\td2rq:belongsToClassMap " + "map:" + subjClassMap + ";");
            pw.println("\td2rq:property " + predicate + ";");
            pw.println("\td2rq:refersToClassMap " + "map:" + objClassMap + ";");
            pw.println("\td2rq:join \"" + join.foreignTable + "." + join.foreignColumn + " => " + join.primaryTable + "." + join.primaryColumn + "\";");
            pw.println("\t.");
        }
        */
    }
}
