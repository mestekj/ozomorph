import ozobot
import asyncio
from _ozo import DeviceManager
from _ozo import get_robot
import math

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
        await dm.movement.arotate(90 * (math.pi / 180), 90 * (math.pi / 180))
    elif action == 'turnRight':
        await dm.movement.arotate(- 90 * (math.pi / 180), 90 * (math.pi / 180))
    elif action == 'wait':
        await asyncio.sleep(1)

async def set_light_color(r, g, b):
    await dm.light_effects.aset_light_color_rgb(r / 255, g / 255, b / 255, ozobot.Lights.TOP | ozobot.Lights.FRONT_1 | ozobot.Lights.FRONT_3 | ozobot.Lights.FRONT_5)

async def main():
    async with asyncio.TaskGroup() as _tg:
        async def _coroutine_init(agent):
            device_name = f"Evo_{agent}"
            r,g,b = agents[agent][0]
            with dm.use_device(device_name):
                await set_light_color(r, g, b)
                await dm.navigation.aset_line_following_speed(50 / 1000.0)
        
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
