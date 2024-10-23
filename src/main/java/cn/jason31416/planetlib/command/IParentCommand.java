package cn.jason31416.planetlib.command;

import java.util.List;

public interface IParentCommand {
    void registerSubCommand(String name, ICommand command);
}
