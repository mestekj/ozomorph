import ozobot
import asyncio
from _ozo import DeviceManager
from _ozo import get_robot

# To be inserted
agents = ...
# agents = {0: ((255, 0, 0), ['turnLeft', 'goAhead', 'goAhead']), 1: ((255, 0, 0), ['wait', 'turnLeft', 'goAhead']), 2: ((0, 0, 255), ['wait', 'turnLeft', 'goAhead'])}


dm = DeviceManager()

for agent in agents:
    dm.add_device(f"Evo_{agent}", get_robot(f"Evo_{agent}", "Evo"))

async def execute_action(action):
    if action == 'goAhead':
        await dm.navigation.anavigate(ozobot.Directions.FORWARD, follow = True)
    elif action == 'turnLeft':
        await dm.navigation.anavigate(ozobot.Directions.LEFT, follow = False)
    elif action == 'turnRight':
        await dm.navigation.anavigate(ozobot.Directions.RIGHT, follow = False)
    elif action == 'wait':
        await asyncio.sleep(1)

async def set_light_color(r, g, b):
    await dm.light_effects.aset_light_color_rgb(r / 127, g / 127, b / 127, ozobot.Lights.TOP | ozobot.Lights.FRONT_1 | ozobot.Lights.FRONT_3 | ozobot.Lights.FRONT_5)

async def main():
    async with asyncio.TaskGroup() as _tg:
        async def _coroutine_init(agent):
            device_name = f"Evo_{agent}"
            r,g,b = agents[agent][0]
            with dm.use_device(device_name):
                await set_light_color(r, g, b)
        
        async def _execute_action(agent, action):
            device_name = f"Evo_{agent}"
            with dm.use_device(device_name):
                await execute_action(action)

        init_tasks = tuple(_tg.create_task(_coroutine_init(agent)) for agent in agents)
        await asyncio.gather(*init_tasks)

        # Create agents_actions_by_step: list of step_actions where each step_actions is a list of (agent, action) tuples
        max_steps = max(len(agents[agent][1]) for agent in agents) if agents else 0
        agents_actions_by_step = []
        for step in range(max_steps):
            step_actions = [(agent, agents[agent][1][step]) for agent in agents if step < len(agents[agent][1])]
            agents_actions_by_step.append(step_actions)

        # exectue actions step by step, all agents in parallel (in each step)
        for step_actions in agents_actions_by_step:
            tasks = tuple(_tg.create_task(_execute_action(agent, action)) for agent, action in step_actions)
            await asyncio.gather(*tasks)


asyncio.run(main())
