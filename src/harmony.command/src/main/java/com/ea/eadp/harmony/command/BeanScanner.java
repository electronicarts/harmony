/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.cluster.entity.exceptions.MethodParseErrorException;
import com.ea.eadp.harmony.command.annotation.CommandPath;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.ParsedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by VincentZhang on 4/26/2018.
 */

class PathNode {
    private static final Logger logger = LoggerFactory.getLogger(PathNode.class);

    private Map<String, PathNode> childrenPathNodes = new HashMap();
    private boolean isLeaf = false;
    private String pathName;

    private CommandExecutor commandExecutor;

    private PathNode parentPathNode;
    private List<Completers.TreeCompleter.Node> childCompleterNodes = new ArrayList<>();

    public PathNode(PathNode parentPathNode, String pathName) {
        this.parentPathNode = parentPathNode;
        this.pathName = pathName;
    }

    public CommandExecutor getCommandExecutor(){
        if(commandExecutor != null){
            return commandExecutor;
        }

        if(getChildren().size() == 1){
            return ((PathNode)getChildren().toArray()[0]).getCommandExecutor();
        }

        return null;
    }

    public boolean getIsLeaf() {
        return isLeaf;
    }

    public PathNode getOrCreate(String path) {
        PathNode retPath = childrenPathNodes.get(path);
        if (retPath == null) {
            retPath = new PathNode(this, path);
            childrenPathNodes.put(path, retPath);
        }
        return retPath;
    }

    public void setExecutor(CommandExecutor commandExecutor) {
        isLeaf = true;
        this.commandExecutor = commandExecutor;
    }

    public Collection<PathNode> getChildren() {
        return childrenPathNodes.values();
    }

    public List<Completers.TreeCompleter.Node> getChildCompleterNodes() {
        return childCompleterNodes;
    }

    public void mergeCompleterNodeUp() {
        if (this.parentPathNode == null) {
            return;
        }

        if (this.isLeaf) {
            this.parentPathNode.getChildCompleterNodes().add(Completers.TreeCompleter.node(this.pathName));
            this.parentPathNode.mergeCompleterNodeUp();
        } else {
            // All children serviceNodes has been settled
            if (this.getChildCompleterNodes().size() == this.childrenPathNodes.size()) {
                List allArguments = this.getChildCompleterNodes();
                allArguments.add(this.getPathName());
                Completers.TreeCompleter.Node curCompleterNode = Completers.TreeCompleter.node(allArguments.toArray());
                this.parentPathNode.getChildCompleterNodes().add(curCompleterNode);
                this.parentPathNode.mergeCompleterNodeUp();
            }
        }
    }

    public String getPathName() {
        return pathName;
    }

    public PathNode getChildByPathName(String pathName) {
        if (childrenPathNodes.size() == 1) { // If there's only one node, check if this node is a parameter
            String path = (String) childrenPathNodes.keySet().toArray()[0];
            if (CommandExecutor.PARAMPATTERN.matcher(path).find()) { // If this is parameter node, just return the node.
                return childrenPathNodes.get(path);
            }
        }
        return childrenPathNodes.get(pathName);
    }

    public String execute(ParsedLine pl) throws InvocationTargetException, IllegalAccessException {
        return commandExecutor.execute(pl.words());
    }
}

@Component
public class BeanScanner {
    private static final Logger logger = LoggerFactory.getLogger(BeanScanner.class);
    @Autowired
    private ApplicationContext ctx;

    private PathNode pathTreeRoot = new PathNode(null, "unknown");

    public void scanAllBeans() throws MethodParseErrorException {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            logger.debug("Scanning bean:" + beanName);
            Class beanClz = ctx.getBean(beanName).getClass();
            for (Method method : beanClz.getDeclaredMethods()) {
                CommandPath annotation = method.getAnnotation(CommandPath.class);
                if (annotation != null) {
                    String path = annotation.path();
                    String[] subPaths = path.split("/");
                    if (subPaths.length <= 1) {
                        throw new RuntimeException("Run bean configuration of bean:" + beanName + "! CommandPath should " +
                                "has at least one parameter. Current Command Path:" + path);
                    }
                    if (subPaths[0].length() != 0) {
                        throw new RuntimeException("Path should start with /");
                    }

                    PathNode curPathNode = pathTreeRoot;
                    for (int i = 1; i < subPaths.length; i++) {
                        curPathNode = curPathNode.getOrCreate(subPaths[i]);
                    }
                    String[] withoutRootSubPath = Arrays.copyOfRange(subPaths, 1, subPaths.length);
                    CommandExecutor commandExecutor = new CommandExecutor(ctx.getBean(beanName), method,
                            withoutRootSubPath);

                    curPathNode.setExecutor(commandExecutor);
                }
            }
        }
        logger.debug("End of scanning all defined beans.");
    }

    Completer getCompleter() {
        // BFS
        Stack<PathNode> nodeStack = new Stack<>();
        nodeStack.push(pathTreeRoot);

        while (!nodeStack.isEmpty()) {
            PathNode curNode = nodeStack.pop();
            Collection<PathNode> childrenCollection = curNode.getChildren();
            childrenCollection.forEach(
                    (PathNode node) -> nodeStack.push(node)
            );

            curNode.mergeCompleterNodeUp();
        }

        return new Completers.TreeCompleter(pathTreeRoot.getChildCompleterNodes());
    }

    public String execute(ParsedLine pl) throws InvocationTargetException, IllegalAccessException {
        PathNode curNode = pathTreeRoot;
        List<String> words = new ArrayList<>();

        // Delete all empty words
        for (String word : pl.words()) {
            if (word.length() != 0) {
                words.add(word);
            }
        }

        if (words.size() == 0) {
            return "";
        }

        int curIdx = 0;
        while (!curNode.getIsLeaf()) {

            // Input token less than expected, print help
            if(curIdx == words.size()){
                if(curNode.getCommandExecutor() != null)
                    return curNode.getCommandExecutor().getCommandHelp();
                else
                    return "Incomplete command, please use tab to get hints and complete this command.";
            }
            curNode = curNode.getChildByPathName(words.get(curIdx++));
            if (curNode == null) {
                return "Error! Command not found!";
            }
        }

        return curNode.execute(pl);
    }
}
